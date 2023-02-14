package com.pfl.ssfmall.order.service.impl;

import com.pfl.common.to.MemberEntityVo;
import com.pfl.common.utils.R;
import com.pfl.ssfmall.order.config.LoginUserInterceptor;
import com.pfl.ssfmall.order.constant.OrderConstant;
import com.pfl.ssfmall.order.feign.CartFeignService;
import com.pfl.ssfmall.order.feign.MemberFeignService;
import com.pfl.ssfmall.order.feign.WareFeignService;
import com.pfl.ssfmall.order.model.dto.OrderCreateTo;
import com.pfl.ssfmall.order.model.dto.WareSkuLockedTo;
import com.pfl.ssfmall.order.model.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;

import com.pfl.ssfmall.order.model.dao.OrderDao;
import com.pfl.ssfmall.order.entity.OrderEntity;
import com.pfl.ssfmall.order.service.OrderService;

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
                // 金额保存成功保存订单
                saveOrder(order);
                // 创建锁定库存Vo
                WareSkuLockedTo wareSkuLockedVo = new WareSkuLockedTo();
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
                    return respVo;
                } else {
                    // 远程锁定库存失败
                    respVo.setCode(3);
                    return respVo;
                }
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
    }

    private void saveOrder(OrderCreateTo order) {

    }

    private OrderCreateTo createOrder() {
        return null;
    }


}