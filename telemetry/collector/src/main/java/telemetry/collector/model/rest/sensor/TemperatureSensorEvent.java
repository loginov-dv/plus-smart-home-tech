package telemetry.collector.model.rest.sensor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class TemperatureSensorEvent extends SensorEvent {
    @NotNull
    @Max(45)
    @Min(-30)
    private Integer temperatureC;

    @NotNull
    @Max(113)
    @Min(-22)
    private Integer temperatureF;

    @Override
    @NotNull
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public SpecificRecordBase toAvro() {
        TemperatureSensorAvro temperatureSensorAvro = TemperatureSensorAvro.newBuilder()
                .setTemperatureC(this.getTemperatureC())
                .setTemperatureF(this.getTemperatureF())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(this.getId())
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(temperatureSensorAvro)
                .build();
    }
}
