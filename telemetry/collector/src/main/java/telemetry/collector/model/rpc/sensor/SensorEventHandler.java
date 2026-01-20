package telemetry.collector.model.rpc.sensor;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventHandler {
    SensorEventProto.PayloadCase getMessageType();

    SpecificRecordBase toAvro(SensorEventProto sensorEventProto);
}
