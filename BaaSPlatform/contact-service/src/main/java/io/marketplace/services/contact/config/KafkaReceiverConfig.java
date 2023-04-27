package io.marketplace.services.contact.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableKafka
@Configuration
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class KafkaReceiverConfig {

    @Value("${kafka.server}")
    private String bootstrapServers;

    private final KafkaProperties kafkaProperties;

    @Value("${kafka.groupid:wallet-processing-data-group}")
    private String consumerGroupId;

    @Value("${kafka.consumer.number-processor:2}")
    private int numberProcessor;

    private String KAFKA_TIMEOUT = "60000";
    private String KAFKA_COMMIT_INTERVAL = "5000";
    private String HEARTBEAT_INTERVAL_MS_CONFIG = "20000";

    @Autowired(required = false)
    public KafkaReceiverConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props =
                Optional.ofNullable(kafkaProperties)
                        .map(KafkaProperties::buildConsumerProperties)
                        .orElseGet(HashMap::new);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, KAFKA_TIMEOUT);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, KAFKA_COMMIT_INTERVAL);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS_CONFIG);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(numberProcessor);
        return factory;
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(numberProcessor);
    }
}
