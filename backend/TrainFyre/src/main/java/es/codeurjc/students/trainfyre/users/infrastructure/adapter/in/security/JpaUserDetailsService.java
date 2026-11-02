package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.security;

import es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@PrimaryAdapter
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final SpringDataUserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) {
        var entity = repository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .roles("USER")
                .build();
    }
}
