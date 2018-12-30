package com.pyg.page.service.msg;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class PageMsgListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage=(TextMessage)message;
        try {
            String text = textMessage.getText();//接收到消息
            boolean b = itemPageService.genItemHtml(Long.parseLong(text));//"网页生成结果
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
