package es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "user_alert_subscriptions",
        indexes = {
                @Index(
                        name = "idx_user_alert_subscriptions_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_alert_subscriptions_expiration",
                        columnList = "expiration"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertSubscriptionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_alert_subscription_lines",
            joinColumns = @JoinColumn(name = "subscription_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_subscription_line",
                    columnNames = {"subscription_id", "line_id"}
            )
    )
    @Column(name = "line_id", nullable = false)
    private Set<Long> lines = new HashSet<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "date",
                    column = @Column(name = "start_date", nullable = false)
            ),
            @AttributeOverride(
                    name = "time",
                    column = @Column(
                            name = "start_time",
                            nullable = false,
                            secondPrecision = 6
                    )
            ),
            @AttributeOverride(
                    name = "zoneId",
                    column = @Column(name = "start_zone_id", nullable = false)
            ),
            @AttributeOverride(
                    name = "offsetSeconds",
                    column = @Column(
                            name = "start_offset_seconds",
                            nullable = false
                    )
            )
    })
    private ZonedDateTimeEmbeddable startTime;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "date",
                    column = @Column(name = "end_date", nullable = false)
            ),
            @AttributeOverride(
                    name = "time",
                    column = @Column(
                            name = "end_time",
                            nullable = false,
                            secondPrecision = 6
                    )
            ),
            @AttributeOverride(
                    name = "zoneId",
                    column = @Column(name = "end_zone_id", nullable = false)
            ),
            @AttributeOverride(
                    name = "offsetSeconds",
                    column = @Column(
                            name = "end_offset_seconds",
                            nullable = false
                    )
            )
    })
    private ZonedDateTimeEmbeddable endTime;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_alert_subscription_days",
            joinColumns = @JoinColumn(name = "subscription_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_subscription_day",
                    columnNames = {"subscription_id", "day_of_week"}
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Set<DayOfWeek> daysOfTheWeek = new HashSet<>();

    @Column(name = "expiration", nullable = false, secondPrecision = 6)
    private Instant expiration;

    @Column(name = "expiration_zone_id", nullable = false)
    private String expirationZoneId;

    public AlertSubscriptionEntity(
            AlertSubscription subscription,
            UserEntity user
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.lines = new HashSet<>(subscription.lines());
        this.startTime =
                new ZonedDateTimeEmbeddable(subscription.startTime());
        this.endTime =
                new ZonedDateTimeEmbeddable(subscription.endTime());
        this.daysOfTheWeek =
                new HashSet<>(subscription.daysOfTheWeek());
        this.expiration = subscription.expiration().toInstant();
        this.expirationZoneId =
                subscription.expiration().getZone().getId();
    }

    public AlertSubscription toDomain() {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        days.addAll(daysOfTheWeek);

        return new AlertSubscription(
                new HashSet<>(lines),
                startTime.toDomain(),
                endTime.toDomain(),
                days,
                expiration.atZone(ZoneId.of(expirationZoneId))
        );
    }
}
