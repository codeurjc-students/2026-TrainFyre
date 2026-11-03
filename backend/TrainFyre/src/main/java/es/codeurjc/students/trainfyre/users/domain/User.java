package es.codeurjc.students.trainfyre.users.domain;

import es.codeurjc.students.trainfyre.security.domain.Role;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@AggregateRoot
@Getter
public class User {

    @Identity
    private final UUID id;
    private final Long version;
    private String username;
    private String password;
    private final Set<Role> roles;
    private final List<AlertSubscription> alertSubscriptions;


    private User(UUID id, Long version, String username, String password, Set<Role> roles, List<AlertSubscription> alertSubscriptions){

        this.id = id;
        this.version = version;
        this.username = username;
        this.password = password;
        this.roles = Set.copyOf(roles);
        this.alertSubscriptions = new ArrayList<>(alertSubscriptions);

    }

    public static User createUser(String username, String password){

        Objects.requireNonNull(username, "Username should not be null");
        Objects.requireNonNull(password, "Password should not be null");

        UUID id = UUID.randomUUID();

        return new User(id, null, username, password, Set.of(Role.USER), new ArrayList<>());

    }

    public static User reconstitute(UUID id, Long version, String username, String password, Set<Role> roles, List<AlertSubscription> alertSubscriptions){

        Objects.requireNonNull(id, "id should not be null");
        Objects.requireNonNull(username, "Username should not be null");
        Objects.requireNonNull(password, "Password should not be null");
        Objects.requireNonNull(roles, "roles should not be null");
        Objects.requireNonNull(alertSubscriptions, "alertSubscriptions should not be null");

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("A user must have at least one role");
        }

        return new User(id, version, username, password, roles, alertSubscriptions);
    }


    public void addAlertSubscription(AlertSubscription subscription){
        Objects.requireNonNull(subscription, "Subscription should not be null");

        if (alertSubscriptions.contains(subscription)) {
            throw new IllegalArgumentException(
                    "Subscription already exists for this user"
            );
        }

        alertSubscriptions.add(subscription);
    }

    public void deleteAlertSubscription(AlertSubscription subscription){
        Objects.requireNonNull(subscription, "subscription should not be null");
        if (!alertSubscriptions.remove(subscription)) {
            throw new IllegalArgumentException(
                    "Subscription does not belong to this user"
            );
        }
    }

    public void changeUsername(String newUsername){
        Objects.requireNonNull(newUsername, "username should not be null");
        username = newUsername;
    }

    public void changePassword(String newPassword){
        Objects.requireNonNull(newPassword, "password should not be null");
        password = newPassword;
    }


    public List<AlertSubscription> getAlertSubscriptions() {
        return List.copyOf(alertSubscriptions);
    }


}
