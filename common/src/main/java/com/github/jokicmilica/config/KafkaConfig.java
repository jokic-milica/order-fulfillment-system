package com.github.jokicmilica.config;

import com.github.jokicmilica.constants.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(KafkaTopics.ORDERS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderResultsTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_RESULTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
