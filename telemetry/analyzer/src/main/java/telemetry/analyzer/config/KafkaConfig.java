package telemetry.analyzer.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("kafka")
public class KafkaConfig {
    private String server;
    private Topics topics;
    private HubConsumer hubConsumer;
    private SnapshotConsumer snapshotConsumer;

    @Getter
    @AllArgsConstructor
    public static class Topics {
        private String hubs;
        private String snapshots;
    }

    @Getter
    @AllArgsConstructor
    public static class HubConsumer {
        private String clientId;
        private String groupId;
        private Integer pollDurationMs;
    }

    @Getter
    @AllArgsConstructor
    public static class SnapshotConsumer {
        private String clientId;
        private String groupId;
        private Integer pollDurationMs;
        private Integer manualCommitRecordsCount;
    }
}
