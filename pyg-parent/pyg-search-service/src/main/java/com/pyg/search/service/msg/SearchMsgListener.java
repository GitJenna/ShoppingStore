package com.pyg.search.service.msg;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

public class SearchMsgListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage=(TextMessage)message;
        try {
            //监听到消息
            String text = textMessage.getText();//json字符串
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            //导入到solr索引库
            itemSearchService.importList(itemList);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
