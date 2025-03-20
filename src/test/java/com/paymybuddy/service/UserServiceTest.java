package com.paymybuddy.service;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User user;
    private User user2;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("Malick");
        user.setPassword("MalickPW@");
        user.setEmail("malick@paymybuddy.com");

        user2 = new User();
        user2.setUsername("Linda");
        user2.setPassword("LindaPW@");
        user2.setEmail("linda@paymybuddy.com");
    }
    // create ____________________________________
    @DisplayName("Should register new user")
    @Test
    public void testSignup() {
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        userService.signup(
                user.getUsername(),
                user.getEmail(),
                user.getPassword());

        verify(userRepository).save(
                argThat(user ->
                        user.getUsername().equals("Malick")));
    }

    @BeforeEach
    public void setForEachTest() {
        userRepository.deleteAll();
    }

    @DisplayName("Should create a connection between 2 users")
    @Test
    public void testAddConnection() {
        userRepository.save(user);
        userRepository.save(user2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));

        userService.addConnection(1, 2);

        assertThat(user.getConnections().contains(user2));
        assertThat(user2.getConnections().contains(user));
    }

    @DisplayName("Should reject the addition of same user as connection")
    @Test
    public void testSameUserConnection() {
        userRepository.save(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class,
                () -> userService.addConnection(1, 1));
    }

    // read ______________________________________
    @DisplayName("Should throw appropriate exception messages")
    @Test
    public void testGetUserByIdThrows() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        Exception sourceEx = assertThrows(RuntimeException.class,
                () -> userService.getUserById(1, "s"));
        assertEquals("Utilisateur source non trouvé", sourceEx.getMessage());

        Exception receiverEx = assertThrows(RuntimeException.class,
                () -> userService.getUserById(2, "r"));
        assertEquals("Destinataire non trouvé", receiverEx.getMessage());

        Exception userEx = assertThrows(RuntimeException.class, () -> userService.getUserById(1, "u"));
        assertEquals("Utilisateur non trouvé", userEx.getMessage());
    }

    // update ____________________________________
    @DisplayName("Should update user data")
    @Test
    public void testUpdateUser() {
        user.setId(1);
        when(userRepository.existsById(anyInt())).thenReturn(true);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                invocation.getArgument(0));

        user.setUsername("Momo");
        userService.updateUser(user);

        verify(userRepository).save(user);
    }
    @DisplayName("Should fail to update because user does not exist")
    @Test
    public void testUpdateUserNotFound() {
        when(userRepository.existsById(1)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(user));

        assertEquals("Utilisateur n'existe pas", exception.getMessage());
    }
    // delete ____________________________________
    @DisplayName("Should delete user")
    @Test
    public void testDeleteUser() {
        when(userRepository.existsById(user.getId())).thenReturn(true);

        userService.deleteUser(user);

        verify(userRepository, times(1)).delete(user);
    }
    @DisplayName("Should not find user to delete and throw exception")
    @Test
    public void testDeleteUserNotFound() {
        user.setId(1);
        when(userRepository.existsById(1)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(user));

        assertEquals("Utilisateur n'existe pas", exception.getMessage());
    }
    @DisplayName("Should remove a connection")
    @Test
    public void testRemoveConnection() {
        user.setId(1);
        user2.setId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));

        user.getConnections().add(user2);
        user2.getConnections().add(user);

        userService.removeConnection(user.getId(), user2.getId());

        assertThat(user.getConnections()).isEmpty();
        assertThat(user2.getConnections()).isEmpty();
    }

}
