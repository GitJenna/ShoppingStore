package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 获取cookie里面的购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人" + username);
        //读取cookie购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartListFromCookie = JSON.parseArray(cartListString, Cart.class);

        if (username.equals("anonymousUser")) {//未登录,从cookie中提取购物车
            return cartListFromCookie;
        }
        //已登录,从redis读取购物车
        List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
        if (cartListFromCookie.size() > 0) {//cookie中有值
            //合并购物车
            List<Cart> cartList = cartService.mergeCartList(cartListFromCookie, cartListFromRedis);
            //将购物车存入redis
            cartService.saveCartListToRedis(username, cartList);
            //清除本地cookie
            CookieUtil.deleteCookie(request, response, "cartList");
            return cartList;
        }
        return cartListFromRedis;
    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105")
    public Result addGoodsToCartLIst(Long itemId, Integer num) {
        /*response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //设置可以进行跨域调用这个方法
        // response.setHeader("Access-Control-Allow-Origin", "*"); * 代表所有的地址都可以调用这个方法
        //注意：这里的端口取决于商品详细页所在tomcat容器的端口，我的商品详细页在端口为9100的tomcat
        response.setHeader("Access-Control-Allow-Credentials", "true");
        //是否允许操作cookie，如果不允许，不用写下面这句，如果允许，上面是不可以写*的。*/
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList = findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) {//未登录,保存到cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {//如果是已登录，保存到redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

}
