package com.pfl.ssfmall.order.controller;

import java.util.Arrays;
import java.util.Map;

import com.pfl.ssfmall.order.model.vo.OrderConfirmVo;
import com.pfl.ssfmall.order.model.vo.OrderSubmitVo;
import com.pfl.ssfmall.order.model.vo.SubmitRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.order.model.entity.OrderEntity;
import com.pfl.ssfmall.order.service.OrderService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;


/**
 * 订单
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:11:58
 */
@Slf4j
@RestController
@RequestMapping("/order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    @GetMapping("/hello")
    public R hello() {
        return R.ok().put("data", "hello");
    }


    /**
     * 订单数据提交
     */
    @PostMapping("/orderSubmit")
    public R orderSubmit(OrderSubmitVo vo) {
        SubmitRespVo respVo = orderService.orderSubmit(vo);
        log.error("======================订单创建成功{}:", respVo);
        // 根据vo中定义的状态码来验证
        if (respVo.getCode() == 0) { // 订单创建成功
            // 下单成功返回到支付页

            return R.ok().put("data", respVo);
        } else { // 下单失败
            // 根据状态码验证对应的状态
            String msg = "下单失败";
            switch (respVo.getCode()) {
                case 1:
                    msg += "订单信息过期，请刷新后再次提交";

                    break;
                case 2:
                    msg += "订单商品价格发生变化，请确认后再次提交";

                    break;
                case 3:
                    msg += "库存锁定失败，商品库存不足";

                    break;
            }
            return R.error(msg);
        }
    }

    /**
     * 订单确认页展示数据
     *
     * @return
     */
    @GetMapping("/confirmOrder")
    public R getConfirmOrderData() {
        OrderConfirmVo data = orderService.getConfirmOrderData();
        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id) {
        OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order) {
        orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order) {
        orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //  @RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids) {
        orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
