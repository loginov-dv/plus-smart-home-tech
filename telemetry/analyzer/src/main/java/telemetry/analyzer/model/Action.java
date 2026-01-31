package telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "actions")
@Getter
@Setter
@ToString
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ActionTypeAvro type;

    @Column(name = "value")
    private Integer value;

    @OneToMany(mappedBy = "action", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<ScenarioAction> scenarioActions = new HashSet<>();
}
