package es.codeurjc.students.trainfyre.users.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AggregateRoot
@Getter
public class User {

    @Identity
    private final UUID id;
    private String username;
    private final String password;
    private final List<AlertSubscription> alertSubscriptions;


    private User(UUID id, String username, String password, List<AlertSubscription> alertSubscriptions){

        this.id = id;
        this.username = username;
        this.password = password;
        this.alertSubscriptions = alertSubscriptions;

    }

    public static User createUser(String username, String password){

        Objects.requireNonNull(username, "Username should not be null");
        Objects.requireNonNull(password, "Password should not be null");

        UUID id = UUID.randomUUID();

        return new User(id, username, password, new ArrayList<>());

    }

    public void addAlertSubscription(AlertSubscription subscription){
        Objects.requireNonNull(subscription, "Subscription should not be null");
        alertSubscriptions.add(subscription);
    }

    public void changeUsername(String newUsername){
        Objects.requireNonNull(newUsername, "username should not be null");
        username = newUsername;
    }

    public void changePassword(String newPassword){}


    public List<AlertSubscription> getAlertSubscriptions() {
        return List.copyOf(alertSubscriptions);
    }


}
