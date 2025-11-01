package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.dto.TradeMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TradeQueueService {

    private static final Logger logger = LoggerFactory.getLogger(TradeQueueService.class);

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;
    private final String queueName;

    public TradeQueueService(
            SqsTemplate sqsTemplate,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.trade-queue-name:dhbw-broker-trades}") String queueName
    ) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    public void sendTradeMessage(TradeMessage message) {
        try {
            String messageBody = objectMapper.writeValueAsString(message);

            sqsTemplate.send(to -> to
                    .queue(queueName)
                    .payload(messageBody)
                    .header("messageId", message.messageId().toString())
                    .header("userId", message.userId().toString())
                    .header("side", message.side())
            );

            logger.info("Trade message sent to queue: {} {} {} for user {}",
                    message.side(), message.quantity(), message.assetSymbol(), message.userEmail());

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize trade message", e);
            throw new RuntimeException("Failed to queue trade message", e);
        } catch (Exception e) {
            logger.error("Failed to send trade message to SQS", e);
            throw new RuntimeException("Failed to queue trade message", e);
        }
    }
}