package telemetry.collector.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class LightSensorEvent extends SensorEvent {
    private Integer linkQuality;
    private Integer luminosity;

    @Override
    @NotNull
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    public SpecificRecordBase toAvro() {
        LightSensorAvro lightSensorAvro = LightSensorAvro.newBuilder()
                .setLinkQuality(this.getLinkQuality())
                .setLuminosity(this.getLuminosity())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(this.getId())
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(lightSensorAvro)
                .build();
    }
}
