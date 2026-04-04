package com.uwindsor.warehouse.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Custom deserializer for OrderEvent that handles both camelCase and snake_case
 * field mapping
 */
public class OrderEventDeserializer implements Deserializer<OrderEvent> {
    private static final Logger log = LoggerFactory.getLogger(OrderEventDeserializer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No special configuration needed
    }

    @Override
    public OrderEvent deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            log.debug("Deserializing OrderEvent from bytes: {}", new String(data));
            OrderEvent event = objectMapper.readValue(data, OrderEvent.class);
            log.debug("Successfully deserialized OrderEvent: id={}, orderId={}, source={}, eventType={}",
                    event.getEventId(), event.getOrderId(), event.getSource(), event.getEventType());
            return event;
        } catch (IOException e) {
            log.error("Error deserializing OrderEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Error deserializing OrderEvent", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}
