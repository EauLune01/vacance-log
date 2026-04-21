package vacance_log.sogang.global.config.rabbitMq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    /* =========================
       Queue
    ========================= */

    public static final String PHOTO_ANALYSIS_QUEUE = "photo.analysis.queue";
    public static final String RECOMMENDATION_QUEUE = "recommendation.queue";

    @Bean
    public Queue photoAnalysisQueue() {
        return new Queue(PHOTO_ANALYSIS_QUEUE, true);
    }

    @Bean
    public Queue recommendationQueue() {
        return new Queue(RECOMMENDATION_QUEUE, true);
    }


    /* =========================
       Exchange
    ========================= */

    // 사진 이벤트용
    public static final String PHOTO_EXCHANGE = "photo.exchange";

    // 여행/추천 이벤트용
    public static final String TRAVEL_EXCHANGE = "travel.exchange";

    @Bean
    public TopicExchange photoExchange() {
        return new TopicExchange(PHOTO_EXCHANGE);
    }

    @Bean
    public TopicExchange travelExchange() {
        return new TopicExchange(TRAVEL_EXCHANGE);
    }


    /* =========================
       Routing Key
    ========================= */

    // 사진 관련 이벤트
    public static final String PHOTO_UPLOADED = "photo.uploaded";

    // 위치 기반 이벤트
    public static final String LOCATION_DETECTED = "location.detected";


    /* =========================
       Binding
    ========================= */

    // 사진 → 분석 큐
    @Bean
    public Binding bindingPhoto(Queue photoAnalysisQueue, TopicExchange photoExchange) {
        return BindingBuilder.bind(photoAnalysisQueue)
                .to(photoExchange)
                .with(PHOTO_UPLOADED);
    }

    // 위치 → 추천 큐
    @Bean
    public Binding bindingRecommendation(Queue recommendationQueue, TopicExchange travelExchange) {
        return BindingBuilder.bind(recommendationQueue)
                .to(travelExchange)
                .with(LOCATION_DETECTED);
    }


    /* =========================
       Message Converter
    ========================= */

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    /* =========================
       RabbitTemplate
    ========================= */

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
