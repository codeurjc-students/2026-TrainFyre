package es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.users.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_username",
                columnNames = "username"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Version
    private Long version;

    @Setter
    @Column(nullable = false)
    private String username;

    @Setter
    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_user_role",
                    columnNames = {"user_id", "role_name"}
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderColumn(name = "subscription_position")
    private List<AlertSubscriptionEntity> alertSubscriptions =
            new ArrayList<>();

    public UserEntity(User user) {
        this.id = user.getId();
        this.version = user.getVersion();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.roles = new HashSet<>(user.getRoles());

        user.getAlertSubscriptions().forEach(subscription ->
                alertSubscriptions.add(
                        new AlertSubscriptionEntity(subscription, this)
                )
        );
    }

    public User toDomain() {
        var subscriptions = alertSubscriptions.stream()
                .map(AlertSubscriptionEntity::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));

        return User.reconstitute(
                id,
                version,
                username,
                password,
                roles,
                subscriptions
        );
    }
}
