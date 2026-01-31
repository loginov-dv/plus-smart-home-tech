package telemetry.aggregator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("kafka")
public class KafkaConfig {
    private String server;
    private Topics topics;
    private Consumer consumer;

    @Getter
    @AllArgsConstructor
    public static class Topics {
        private String sensors;
        private String snapshots;
    }

    @Getter
    @AllArgsConstructor
    public static class Consumer {
        private String clientId;
        private String groupId;
        private Integer pollDurationMs;
    }
}
