package com.pyg.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import com.pyg.pojo.TbSeckillGoodsExample.Criteria;
/*
     Seconds   Minutes   Hours   DayofMonth   Month    DayofWeek
        秒        分      小时     月中的天      月     星期中的天

	 0 0 10,14,16 * * ? 每天上午10点，下午2点，4点
	 0 0/30 9-17 * * ?  朝九晚五工作时间内每半小时
	 0 0 12 ? * WED 表示每个星期三中午12点
	"0 0 12 * * ?"  每天中午12点触发
	"0 15 10 ? * *" 每天上午10:15触发
	"0 15 10 * * ?" 每天上午10:15触发
	"0 15 10 * * ? *" 每天上午10:15触发
	"0 15 10 * * ? 2005" 2005年的每天上午10:15触发
	"0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发
	"0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发
	"0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
	"0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发
	"0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发
	"0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发
	"0 15 10 15 * ?" 每月15日上午10:15触发
	"0 15 10 L * ?" 每月最后一日的上午10:15触发
	"0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发
	"0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发
	"0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发

	              #:用于确定每个月第几个星期几，只能出现在DayofMonth域

 */
@Component
public class SeckillTask {

	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	@Scheduled(cron="0 * * * * ?")
	public void refreshSeckillGoods(){
		System.out.println("执行了秒杀商品增量更新 任务调度"+new Date());
		
		//查询缓存中的秒杀商品ID集合
		List goodsIdList =  new ArrayList( redisTemplate.boundHashOps("seckillGoods").keys());
		System.out.println(goodsIdList);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");// 审核通过的商品
		criteria.andStockCountGreaterThan(0);//库存数大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());//开始日期小于等于当前日期
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());//截止日期大于等于当前日期
		
		if(goodsIdList.size()>0){
			criteria.andIdNotIn(goodsIdList);//排除缓存中已经存在的商品ID集合
		}
				
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
		//将列表数据装入缓存 
		for(TbSeckillGoods seckillGoods:seckillGoodsList){
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			System.out.println("增量更新秒杀商品ID:"+seckillGoods.getId());
		}	
		System.out.println(".....end....");
		
	}
	
	
	
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods(){
		//查询出缓存中的数据，扫描每条记录，判断时间，如果当前时间超过了截止时间，移除此记录
		List<TbSeckillGoods> seckillGoodsList= redisTemplate.boundHashOps("seckillGoods").values();
		System.out.println("执行了清除秒杀商品的任务"+new Date());
		for(TbSeckillGoods seckillGoods :seckillGoodsList){
			if(seckillGoods.getEndTime().getTime() < new Date().getTime() ){
				//同步到数据库
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);				
				//清除缓存
				redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
				System.out.println("秒杀商品"+seckillGoods.getId()+"已过期");
								
			}			
		}		
		System.out.println("执行了清除秒杀商品的任务...end");
	}
	
}
