package telemetry.collector.model.rest.sensor;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class ClimateSensorEvent extends SensorEvent {
    @NotNull
    @Max(45)
    @Min(-40)
    private Integer temperatureC;

    @NotNull
    @PositiveOrZero
    @Max(100)
    private Integer humidity;

    @NotNull
    @Positive
    private Integer co2Level;

    @Override
    @NotNull
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public SpecificRecordBase toAvro() {
        ClimateSensorAvro climateSensorAvro = ClimateSensorAvro.newBuilder()
                .setTemperatureC(this.getTemperatureC())
                .setHumidity(this.getHumidity())
                .setCo2Level(this.getCo2Level())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(this.getId())
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(climateSensorAvro)
                .build();
    }
}
