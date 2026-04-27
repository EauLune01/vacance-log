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

    /**
     * Queue Names
     */
    public static final String PHOTO_ANALYSIS_QUEUE = "photo.analysis.queue";
    public static final String RECOMMENDATION_QUEUE = "recommendation.queue";
    public static final String DIARY_GENERATE_QUEUE = "diary.generate.queue";

    /**
     * Exchange Names
     */
    public static final String PHOTO_EXCHANGE = "photo.exchange";
    public static final String TRAVEL_EXCHANGE = "travel.exchange";

    /**
     * Routing Keys
     */
    public static final String PHOTO_UPLOADED = "photo.uploaded";      // 사진 업로드 이벤트
    public static final String LOCATION_DETECTED = "location.detected"; // 위치 감지 (추천) 이벤트
    public static final String DIARY_GENERATE = "diary.generate"; // 다이어리 생성 이벤트

    /* -------------------------------------------------------------------------- */

    /**
     * 1. Queue Beans
     */
    @Bean
    public Queue photoAnalysisQueue() { return new Queue(PHOTO_ANALYSIS_QUEUE, true); }

    @Bean
    public Queue recommendationQueue() { return new Queue(RECOMMENDATION_QUEUE, true); }

    @Bean
    public Queue diaryGenerateQueue() { return new Queue(DIARY_GENERATE_QUEUE, true); }

    /**
     * 2. Exchange Beans (Topic Type)
     */
    @Bean
    public TopicExchange photoExchange() { return new TopicExchange(PHOTO_EXCHANGE); }

    @Bean
    public TopicExchange travelExchange() { return new TopicExchange(TRAVEL_EXCHANGE); }

    /**
     * 3. Bindings
     */
    // [사진 서비스] 사진 업로드 -> AI 분석 큐
    @Bean
    public Binding bindingPhoto(Queue photoAnalysisQueue, TopicExchange photoExchange) {
        return BindingBuilder.bind(photoAnalysisQueue)
                .to(photoExchange)
                .with(PHOTO_UPLOADED);
    }

    // [여행 서비스] 위치 감지 -> 장소 추천 큐
    @Bean
    public Binding bindingRecommendation(Queue recommendationQueue, TopicExchange travelExchange) {
        return BindingBuilder.bind(recommendationQueue)
                .to(travelExchange)
                .with(LOCATION_DETECTED);
    }

    // [여행 서비스] 여행 종료 -> 다이어리 생성 큐
    @Bean
    public Binding bindingDiary(Queue diaryGenerateQueue, TopicExchange travelExchange) {
        return BindingBuilder.bind(diaryGenerateQueue)
                .to(travelExchange)
                .with(DIARY_GENERATE);
    }

    /**
     * 4. Infrastructure (Converter, Template)
     */
    //객체 ↔ JSON 변환기
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //RabbitMQ로 메시지 보내는 “클라이언트”
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
