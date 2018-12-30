package com.pyg.user.service;
import com.pyg.pojo.TbUser;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface UserService {

	/**
	 * 增加
	*/
	public void add(TbUser user);
	
	
	/**
	 * 修改
	 */
	public void update(TbUser user);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbUser findOne(Long id);
	

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbUser user, int pageNum, int pageSize);

	/**
	 * 发送短信验证码
	 * @param phone
	 */
	void createSmsCode(String phone);

	/**
	 * 校验验证码
	 * @param phone
	 * @param smscode
	 * @return
	 */
	boolean checkSmsCode(String phone, String smscode);
}
