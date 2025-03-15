package com.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import java.util.Set;
import com.paymybuddy.model.User;
import org.junit.jupiter.api.DisplayName;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("Malick");
        user1.setEmail("Malick@example.com");
        user1.setPassword("MalickPW@@");
        userRepository.save(user1);

        user2 = new User();
        user2.setUsername("Linda");
        user2.setEmail("Linda@example.com");
        user2.setPassword("LindaPW@@");
        userRepository.save(user2);
    }

    @DisplayName("Should find the user by username")
    @Test
    void testFindByUsername() {
        // Given : (user created in setUp)
        // When
        Optional<User> foundUser = userRepository.findByUsername("Malick");
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("Malick");
    }

    @DisplayName("Should find the user by email")
    @Test
    void testFindByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("Malick@example.com");
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("Malick@example.com");
    }

    @DisplayName("Should find 1 connection")
    @Test
    void testFindConnectionsByUserId() {
        // Given
        user1.getConnections().add(user2);
        user2.getConnections().add(user1);
        userRepository.save(user2);

        // When
        Set<User> connections = userRepository.findConnectionsByUserId(user1.getId());

        // Then
        assertThat(connections).isNotNull();
        assertThat(connections.size()).isEqualTo(1);
        assertThat(connections).contains(user2);
    }

}
