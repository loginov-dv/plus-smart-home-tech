package telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@ToString
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ConditionTypeAvro type;

    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    private ConditionOperationAvro operation;

    @Column(name = "value")
    private Integer value;

    @OneToMany(mappedBy = "condition", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<ScenarioCondition> scenarioConditions = new HashSet<>();
}
