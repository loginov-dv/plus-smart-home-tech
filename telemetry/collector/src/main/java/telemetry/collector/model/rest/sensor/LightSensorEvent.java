package telemetry.collector.model.rest.sensor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @PositiveOrZero
    private Integer linkQuality;

    @PositiveOrZero
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
