package com.pyg.user.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbSeller;
import com.pyg.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Component("userDetailService")
public class UserDetailServiceImpl implements UserDetailsService {

    //@Reference
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {

        //提交表单进行登录的时候，根据提交的username-parameter指定的参数值,从数据库查询TbSeller对象
        TbSeller seller = sellerService.findOne(sellerId);
        //只有审核通过才去校验密是否正确
        if ("1".equals(seller.getStatus())) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            //<security:intercept-url pattern="/**" access="ROLE_SELLER" />
            //给当前用户ROLE_SELLER权限
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
            //这里返回的User对象的密码会和表单提交的密码进行比对
            User user = new User(seller.getSellerId(), seller.getPassword(), authorities);
            return user;
        }

        return null;
    }

}
