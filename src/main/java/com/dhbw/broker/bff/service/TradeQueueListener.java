package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.dto.TradeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TradeQueueListener {

    private static final Logger logger = LoggerFactory.getLogger(TradeQueueListener.class);

    private final TradeExecutionService tradeExecutionService;
    private final ObjectMapper objectMapper;

    public TradeQueueListener(TradeExecutionService tradeExecutionService, ObjectMapper objectMapper) {
        this.tradeExecutionService = tradeExecutionService;
        this.objectMapper = objectMapper;
    }

    @SqsListener("${aws.sqs.trade-queue-name:dhbw-broker-trades}")
    public void receiveTradeMessage(
            @Payload String messageBody
    ) {
        try {
            logger.info("Received trade message from SQS queue");

            TradeMessage message = objectMapper.readValue(messageBody, TradeMessage.class);

            logger.info("Parsed trade message: {} {} {} for user {}",
                    message.side(), message.quantity(), message.assetSymbol(), message.userEmail());

            // Trade ausführen
            tradeExecutionService.executeTradeFromQueue(message);

            logger.info("Trade message processed successfully");

        } catch (ResponseStatusException e) {
            
            logger.error("Business error processing trade: {} - Message will be discarded", e.getReason(), e);
         

        } catch (Exception e) {
            
            logger.error("Technical error processing trade message - will retry", e);
            throw new RuntimeException("Failed to process trade message", e);
        }
    }
}