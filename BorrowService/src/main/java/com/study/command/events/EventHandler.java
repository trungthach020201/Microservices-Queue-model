package com.study.command.events;

import com.study.EventDrivent.BorrowEvent;
import com.study.command.data.Borrow;
import com.study.command.data.BorrowReporitory;
import com.study.kafkaConfig.KafkaConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventHandler {

    private final BorrowReporitory borrowReporitory;
    private final RabbitTemplate template;
    @org.axonframework.eventhandling.EventHandler
    public void on (BorrowCreatedEvent event){
        Borrow borrow = new Borrow();
        BeanUtils.copyProperties(event,borrow);
        borrowReporitory.save(borrow);
        //publish event to toppic
        BorrowEvent pubEvent = new BorrowEvent("Borrow", borrow.getBookId());
        template.convertAndSend("book_exchange_6", KafkaConfiguration.ROUTING_KEY, pubEvent);
        System.out.println("==============> Pubpish event" + pubEvent);
    }
}
