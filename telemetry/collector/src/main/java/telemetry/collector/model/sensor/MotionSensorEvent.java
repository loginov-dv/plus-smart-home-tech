package telemetry.collector.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class MotionSensorEvent extends SensorEvent {
    @NotNull
    private Integer linkQuality;
    @NotNull
    private Boolean motion;
    @NotNull
    private Integer voltage;

    @Override
    @NotNull
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    public SpecificRecordBase toAvro() {
        MotionSensorAvro motionSensorAvro = MotionSensorAvro.newBuilder()
                .setLinkQuality(this.getLinkQuality())
                .setMotion(this.getMotion())
                .setVoltage(this.getVoltage())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(this.getId())
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(motionSensorAvro)
                .build();
    }
}
