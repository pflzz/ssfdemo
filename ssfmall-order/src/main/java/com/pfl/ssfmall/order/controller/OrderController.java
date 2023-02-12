package com.pfl.ssfmall.order.controller;

import java.util.Arrays;
import java.util.Map;

import com.pfl.ssfmall.order.vo.OrderConfirmVo;
import com.pfl.ssfmall.order.vo.OrderSubmitVo;
import com.pfl.ssfmall.order.vo.SubmitRespVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.order.entity.OrderEntity;
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
@RestController
    @RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    /**
     * 订单数据提交
     */
    @PostMapping("/orderSubmit")
    public R orderSubmit(OrderSubmitVo vo) {
        SubmitRespVo respVo = orderService.orderSubmit(vo);
        if (respVo.getCode() == 0) {
            return R.ok();
        } else {
            return R.error();
        }
    }
    /**
     * 订单确认页跳转
     * @return
     */
    @GetMapping("confirmOrder")
    public R getConfirmOrderData() {
        OrderConfirmVo data = orderService.getConfirmOrderData();
        return R.ok().put("data", data);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
