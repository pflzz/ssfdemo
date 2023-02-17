package com.pfl.ssfmall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.pfl.common.to.MemberEntityVo;
import com.pfl.common.utils.R;
import com.pfl.ssfmall.order.config.LoginUserInterceptor;
import com.pfl.ssfmall.order.constant.OrderConstant;
import com.pfl.ssfmall.order.feign.CartFeignService;
import com.pfl.ssfmall.order.feign.MemberFeignService;
import com.pfl.ssfmall.order.feign.ProductFeignService;
import com.pfl.ssfmall.order.feign.WareFeignService;
import com.pfl.ssfmall.order.model.dto.FareTo;
import com.pfl.ssfmall.order.model.dto.OrderCreateTo;
import com.pfl.ssfmall.order.model.dto.WareSkuLockedTo;
import com.pfl.ssfmall.order.model.entity.OrderItemEntity;
import com.pfl.ssfmall.order.model.vo.*;
import com.pfl.ssfmall.order.service.OrderItemService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;

import com.pfl.ssfmall.order.model.dao.OrderDao;
import com.pfl.ssfmall.order.model.entity.OrderEntity;
import com.pfl.ssfmall.order.service.OrderService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private OrderItemService orderItemService;

    public static final ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo getConfirmOrderData() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        MemberEntityVo member = LoginUserInterceptor.threadLocal.get();
        Long memberId = member.getId();
        // 1. 远程查询用户的收货地址信息
        List<MemberAddressVo> address = memberFeignService.getAddress(memberId);
        orderConfirmVo.setAddress(address);
        // 2. 远程查询用户的购物车条目信息
        List<OrderItemVo> cartItems = cartFeignService.getCartItems();
        orderConfirmVo.setItems(cartItems);
        // 3. 获取用户积分信息
        Integer integration = member.getIntegration();
        orderConfirmVo.setIntegration(integration);
        // 4. 通过重写实体类的 get 方法获取总价


        // 防重令牌
        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        return orderConfirmVo;

    }


    @GlobalTransactional
    @Override
    public SubmitRespVo orderSubmit(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);
        SubmitRespVo respVo = new SubmitRespVo();
        MemberEntityVo member = LoginUserInterceptor.threadLocal.get();
        String orderToken = vo.getOrderToken();
        // 验证令牌（原子性） 使用 redis 脚本
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEY[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()), orderToken);
        if (result == 1L) {
            // 验证成功
            // 1. 创建订单，订单项信息
            OrderCreateTo order = createOrder();
            // 2. 应付总额
            BigDecimal payAmount = order.getOrder().getPayAmount();
            // 应付价格
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 创建锁定库存Vo todo 先锁库存，不锁库存创建订单什么的都没有意义
                WareSkuLockedTo wareSkuLockedVo = new WareSkuLockedTo();
                // 远程调用库存服务锁定库存
                R r = wareFeignService.orderLockStock(wareSkuLockedVo);
                if (r.getCode() == 0) { // 库存锁定成功
                    // 将订单对象放到返回Vo中
                    respVo.setOrder(order.getOrder());
                    // 设置状态码
                    respVo.setCode(0);
                    // 订单创建成功发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange"
                            ,"order.create.order"
                            ,order.getOrder());

                } else {
                    // 远程锁定库存失败
                    respVo.setCode(3);
                    return respVo;
                }
                // 金额保存成功保存订单
                saveOrder(order);

                // 准备好商品项
                List<OrderItemVo> lock = order.getOrderItems().stream().map(orderItemEntity -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    // 商品购买数量
                    orderItemVo.setCount(orderItemEntity.getSkuQuantity());
                    // skuid 用来查询商品信息
                    orderItemVo.setSkuId(orderItemEntity.getSkuId());
                    // 商品标题
                    orderItemVo.setTitle(orderItemEntity.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                // 订单号
                wareSkuLockedVo.setOrderSn(order.getOrder().getOrderSn());
                // 商品项
                wareSkuLockedVo.setLocks(lock);

            } else {
                // 商品价格比较失败
                respVo.setCode(2);
                return respVo;
            }
        } else {
            // 验证失败
            respVo.setCode(1);
            return respVo;
        }
        return respVo;
    }

    private void saveOrder(OrderCreateTo order) {
        save(order.getOrder());
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrder(orderSn);
        orderCreateTo.setOrder(order);
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        computePrice(orderItemEntities, order);
        orderCreateTo.setOrderItems(orderItemEntities);
        return null;
    }

    private OrderEntity computePrice(List<OrderItemEntity> orderItemEntities, OrderEntity order) {
        // 1、定义好相关金额，然后遍历购物项进行计算
        // 总价格
        BigDecimal total = new BigDecimal("0");
        //相关优惠信息
        // 优惠卷抵扣金额
        BigDecimal coupon = new BigDecimal("0");
        // 积分优惠金额
        BigDecimal integration = new BigDecimal("0");
        // 促销优惠金额
        BigDecimal promotion = new BigDecimal("0");
        // 积分
        BigDecimal gift = new BigDecimal("0");
        // 成长值
        BigDecimal growth = new BigDecimal("0");

        // 遍历订单项将所有的优惠信息进行相加
        for (OrderItemEntity itemEntity : orderItemEntities) {
            coupon = coupon.add(itemEntity.getCouponAmount()); // 优惠卷抵扣
            integration = integration.add(itemEntity.getIntegrationAmount()); // 积分优惠分解金额
            promotion = promotion.add(itemEntity.getPromotionAmount()); // 商品促销分解金额
            gift = gift.add(new BigDecimal(itemEntity.getGiftIntegration().toString())); // 赠送积分
            growth = growth.add(new BigDecimal(itemEntity.getGiftGrowth())); // 赠送成长值
            total = total.add(itemEntity.getRealAmount()); //优惠前的总金额
        }

        // 2、设置订单金额
        // 订单总金额
        order.setTotalAmount(total);
        // 应付总额 = 订单总额 + 运费信息
        order.setPayAmount(total.add(order.getFreightAmount()));
        // 促销优化金额（促销价、满减、阶梯价）
        order.setPromotionAmount(promotion);
        // 优惠券抵扣金额
        order.setCouponAmount(coupon);

        // 3、设置积分信息
        // 订单购买后可以获得的成长值
        order.setGrowth(growth.intValue());
        // 积分抵扣金额
        order.setIntegrationAmount(integration);
        // 可以获得的积分
        order.setIntegration(gift.intValue());
        // 删除状态【0->未删除；1->已删除】
        order.setDeleteStatus(0);
        return order;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItem = cartFeignService.getCartItems();
        if (currentUserCartItem != null || currentUserCartItem.size() > 0) {
            List<OrderItemEntity> collect = currentUserCartItem.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1、根据skuid查询关联的spuinfo信息
        Long skuId = cartItem.getSkuId();
        R spuinfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = spuinfo.getData(new TypeReference<SpuInfoVo>() {
        });
        // 2、设置商品项spu信息
        // 品牌信息
        itemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        // 商品分类信息
        itemEntity.setCategoryId(spuInfoVo.getCatalogId());
        // spuid
        itemEntity.setSpuId(spuInfoVo.getId());
        // spu_name 商品名字
        itemEntity.setSpuName(spuInfoVo.getSpuName());

        // 3、设置商品sku信息
        // skuid
        itemEntity.setSkuId(skuId);
        // 商品标题
        itemEntity.setSkuName(cartItem.getTitle());
        // 商品图片
        itemEntity.setSkuPic(cartItem.getImage());
        // 商品sku价格
        itemEntity.setSkuPrice(cartItem.getPrice());
        // 商品属性以 ; 拆分
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        // 商品购买数量
        itemEntity.setSkuQuantity(cartItem.getCount());

        // 4、设置商品优惠信息【不做】
        // 5、设置商品积分信息
        // 赠送积分 移弃小数值
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        // 赠送成长值
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        // 6、订单项的价格信息
        // 这里需要计算商品的分解信息
        // 商品促销分解金额
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        // 优惠券优惠分解金额
        itemEntity.setCouponAmount(new BigDecimal("0"));
        // 积分优惠分解金额
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        // 商品价格乘以商品购买数量=总金额(未包含优惠信息)
        BigDecimal origin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        // 总价格减去优惠卷-积分优惠-商品促销金额 = 总金额
        origin.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        // 该商品经过优惠后的分解金额
        itemEntity.setRealAmount(origin);
        return itemEntity;
    }

    private OrderEntity buildOrder(String orderSn) {
        // 拿到共享数据
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        // 用户登陆数据
        MemberEntityVo memberEntityVo = LoginUserInterceptor.threadLocal.get();
        // 订单实体
        OrderEntity order = new OrderEntity();
        order.setMemberId(memberEntityVo.getId());
        order.setOrderSn(orderSn);
        order.setCreateTime(new Date());
        // 远程查询用户收货地址信息
        R fare = wareFeignService.getFare(orderSubmitVo.getAddressId());
        if (fare.getCode() == 0) {
            FareTo data = fare.getData(new TypeReference<FareTo>() {
            });
            //将查询到的会员收货地址信息设置到订单对象中
            // 运费金额
            order.setFreightAmount(data.getFare());
            // 城市
            order.setReceiverCity(data.getMemberAddressVo().getCity());
            // 详细地区
            order.setReceiverDetailAddress(data.getMemberAddressVo().getDetailAddress());
            // 收货人姓名
            order.setReceiverName(data.getMemberAddressVo().getName());
            // 收货人手机号
            order.setReceiverPhone(data.getMemberAddressVo().getPhone());
            // 区
            order.setReceiverRegion(data.getMemberAddressVo().getRegion());
            // 省份直辖市
            order.setReceiverProvince(data.getMemberAddressVo().getProvince());
        }
        return order;
    }


}