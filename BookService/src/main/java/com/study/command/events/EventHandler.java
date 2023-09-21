package com.study.command.events;

import com.study.command.data.Book;
import com.study.command.data.BookRepository;
import com.study.EventDrivent.BorrowEvent;
//import com.study.kafkaConfig.KafkaConfiguration;
import com.study.kafkaConfig.KafkaConfiguration;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
@RequiredArgsConstructor
@ProcessingGroup("book")
public class EventHandler {

    private final BookRepository bookRepository;
    @org.axonframework.eventhandling.EventHandler
    public void on (BookCreatedEvent event) throws Exception {
        Book book = new Book();
        BeanUtils.copyProperties(event,book);
        bookRepository.save(book);
//        throw new Exception("Exception Occurred");
    }

    @org.axonframework.eventhandling.EventHandler
    public void on (BookUpdatedEvent event) throws Exception {
        Book book = bookRepository.findById(event.getBookId()).orElseThrow(()-> new NotFoundException("Not Found This Book"));
        BeanUtils.copyProperties(event,book);
        bookRepository.save(book);
//        throw new Exception("Exception Occurred");
    }
    @org.axonframework.eventhandling.EventHandler
    public void on (BookDeletedEvent event) throws Exception {
        Book book = bookRepository.findById(event.getBookId()).orElseThrow(()-> new NotFoundException("Not Found This Book"));
        bookRepository.deleteById(book.getBookId());
//        throw new Exception("Exception Occurred");
    }
    @org.axonframework.eventhandling.EventHandler
    public void on (DeletedAllBookEvent event) throws Exception {
        bookRepository.deleteAll();
//        throw new Exception("Exception Occurred");
    }
    @RabbitListener(queues = KafkaConfiguration.QUEUE)
    public void processUpdateCapacity(BorrowEvent borrowEvent){
        System.out.println("==============> Receive Event" + borrowEvent.getBookId());
    Book book = bookRepository.findById(borrowEvent.getBookId()).get();
        System.out.println("Book is"+ book.getBookId());
        if (borrowEvent.getEventType().equals("Borrow")){
            book.setCapacity(book.getCapacity()-1);
            bookRepository.save(book);
        }
        if (borrowEvent.getEventType().equals("Return")){
            book.setCapacity(book.getCapacity()+1);
            bookRepository.save(book);
        }
    }

    @ExceptionHandler
    public void handle(Exception exception) throws Exception {
        throw exception;
    }
}
