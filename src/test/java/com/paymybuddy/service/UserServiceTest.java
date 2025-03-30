package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.model.UserProfileDTO;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
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
        // Arrange
        when(userRepository.existsById(user.getId())).thenReturn(true);

        List<Transaction> transactions = new ArrayList<>();
        when(transactionRepository.findByUser(user)).thenReturn(transactions);

        User connection1 = new User();
        connection1.setId(2);
        User connection2 = new User();
        connection2.setId(3);
        Set<User> connections = new HashSet<>();
        connections.add(connection1);
        connections.add(connection2);
        user.setConnections(connections);

        User otherUser = new User();
        otherUser.setId(4);
        otherUser.setConnections(new HashSet<>(Collections.singletonList(user)));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, otherUser));

        // Act
        userService.deleteUser(user);

        // Assert
        verify(userRepository, times(1))
                .delete(user);
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
    // password __________________________________
    @DisplayName("should hash password upon creating user")
    @Test
    public void testHashPassword() {
        String hashedPassword = "HashedPassword";

        when(passwordEncoder.encode(anyString()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                invocation.getArgument(0));

        userService.signup(
                user.getUsername(),
                user.getEmail(),
                user.getPassword());

        verify(passwordEncoder).encode(user.getPassword());
        verify(userRepository).save(
                argThat(newUser ->
                        !newUser.getPassword().equals(user.getPassword())
                        && newUser.getPassword().equals(hashedPassword)
        ));
    }

    @DisplayName("should hash password upon updating password")
    @Test
    public void testHashPasswordUpdate() {
        String newPassword = "NewPassword";
        String hashedPassword = "NewHashedPassword";
        user.setId(1);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString()))
                .thenReturn(hashedPassword);

        userService.updatePassword(1, newPassword);

        assertThat(user.getPassword()).isEqualTo(hashedPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }

    @DisplayName("should validate password")
    @Test
    public void testCheckPassword() {
        String password = user.getPassword();
        String hashedPassword = "$2a$12$FYqf.0c9wqzwzlPElWP8iuGUDxk88r7PIw/ole73yJ2AzFxzrznqe";

        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
        when(passwordEncoder.matches("1234Password", hashedPassword)).thenReturn(false);

        assertThat(userService.checkPassword(password, hashedPassword)).isTrue();
        assertThat(userService.checkPassword("1234Password", hashedPassword)).isFalse();

        verify(passwordEncoder, times(2)).matches(anyString(), eq(hashedPassword));

    }

}
