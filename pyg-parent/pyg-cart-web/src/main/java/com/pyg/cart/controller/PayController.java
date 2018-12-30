package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeChatService;
import com.pyg.pojo.TbPayLog;
import entity.Result;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private WeChatService weChatService;
    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //调用获取支付日志对象的方法，得到订单号和金额
        //1.获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.提取支付日志（从缓存 ）
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);
        //3.调用微信支付接口
        if (payLog != null) {
            return weChatService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }else {
            return new HashMap<>();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int x=0;
        while (true){
            Map<String,String> map=weChatService.queryPayStatus(out_trade_no);//调用查询
            if(map==null){
                result=new Result(false,"支付发生错误");
                break;
            }
            if(map.get("trade_state").contains("SUCCESS")){//支付成功
                result=new Result(true,"支付成功");
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));//修改订单状态
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为5分钟
            x++;
            if (x >= 100) {
                result = new Result(false, "二维码超时");
                break;
            }
        }
        return result;
    }
}
