package com.pyg.search.service.msg;

import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

public class ItemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            //监听获取到消息
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            //执行索引库删除
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
