package com.paymybuddy.service;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // create ____________________________________
    public void signup(String username, String email, String password) {
        String hashPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashPassword);
        userRepository.save(user);
    }

    public void addConnection(Integer userId, Integer connectionId) {
        User user = getUserById(userId, "u");
        User connection = getUserById(connectionId, "u");

        if (user.equals(connection)) {
            throw new IllegalArgumentException("Utilisateur ne peut pas s'ajouter");
        }

        user.getConnections().add(connection);
        userRepository.save(user);

        connection.getConnections().add(user);
        userRepository.save(connection);
    }

    // read ____________________________________
    public User getUserById(Integer id, String u) {

        String eMsg = switch (u) {
            case "s" -> "Utilisateur source non trouvé";
            case "r" -> "Destinataire non trouvé";
            default  -> "Utilisateur non trouvé";
        };
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(eMsg)
                );
    }

    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findConnection(Integer userId, Integer connectionId) {
        return userRepository.findConnectionsByUserId(userId)
                .stream()
                .filter(connection ->
                        connection.getId().equals(connectionId))
                .findFirst();
    }

    public Set<User> getAllConnections(Integer userId) {
        return userRepository.findConnectionsByUserId(userId);
    }

    // update __________________________________
    public void updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Utilisateur n'existe pas");
        }
        userRepository.save(user);
    }

    // delete __________________________________
    public void deleteUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Utilisateur n'existe pas");
        }
        userRepository.delete(user);
    }

    public void removeConnection(Integer userId, Integer connectionId) {
        User user = getUserById(userId, "u");
        User connection = getUserById(connectionId, "u");

        user.getConnections().remove(connection);
        userRepository.save(user);

        connection.getConnections().remove(user);
        userRepository.save(connection);
    }

    // password __________________________________
    public void updatePassword(Integer userId, String newPassword) {
        User user = getUserById(userId, "u");
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
    public boolean checkPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
