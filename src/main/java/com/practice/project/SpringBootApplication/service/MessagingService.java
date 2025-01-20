package com.practice.project.SpringBootApplication.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class MessagingService {

    @Value("${spring.kafka.topic.user-image-events}")
    private String TOPIC;// Kafka topic name


    private static final Logger logger = LoggerFactory.getLogger(ImgurService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Async
    public void publishEvent(String username, String imageName) {
        String message = String.format("User: %s, Image: %s", username, imageName);

        logger.info("Starting for publishing the event");

        try{// Send the message and attach a callback to handle success/failure
            kafkaTemplate.send(TOPIC, message).whenComplete(
                    // Success callback
                    (result, ex) -> {
                        if (ex != null) {
                            logger.warn("Error publishing event: " + message + " Error: " + ex.getMessage());
                        } else {
                            logger.debug("Published event: " + message);
                        }
                    }
            );

        }catch(Exception e){
            logger.warn("Error publishing event "+message+"Error: "+e.getMessage());
        }


    }
}

