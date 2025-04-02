package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.model.UserProfileDTO;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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

        Optional<User> connectionExists = findConnection(userId, connection.getEmail());
        if (connectionExists.isPresent()) {
            throw new RuntimeException("La connection existe déjà");
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

    public Optional<User> findConnection(Integer userId, String connectionEmail) {
        return userRepository.findConnectionsByUserId(userId)
                .stream()
                .filter(connection ->
                        connection.getEmail().equals(connectionEmail))
                .findFirst();
    }

    public Set<UserProfileDTO> getAllConnections(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Set<User> connections = user.getConnections();

        Set<UserProfileDTO> connectionsDTO = new HashSet<>();
        for (User connection : connections) {
            connectionsDTO.add(new UserProfileDTO(connection.getUsername(), connection.getEmail(), null));
        }
        return connectionsDTO;
    }

    // update __________________________________
    public void updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Utilisateur n'existe pas");
        }
        userRepository.save(user);
    }

    // delete __________________________________
    @Transactional
    public void deleteUser(User user) {

        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Utilisateur n'existe pas");
        }

        try {
            // delete this user's transactions
            List<Transaction> transactionList = transactionRepository.findByUser(user);
            transactionRepository.deleteAll(transactionList);

            // delete the connections
            Set<User> connections = new HashSet<>(user.getConnections());
            for (User connection : connections) {
                try {
                    removeConnection(user.getId(), connection.getId());
                } catch (Exception e) {
                    log.error("Error removing connection between user {} and connection {}: {}", user.getId(), connection.getId(), e.getMessage());
                }
            }

            // Remove user from other users' connections
            List<User> allUsers = userRepository.findAll();
            for (User otherUser : allUsers) {
                if (otherUser.getConnections().contains(user)) {
                    otherUser.getConnections().remove(user);
                    userRepository.save(otherUser);
                }
            }

            // delete the user
            userRepository.delete(user);
        } catch (Exception e) {
            log.error("Erreur à la suppression de l'utilisateur", e);
            throw new RuntimeException("Erreur à la suppression");
        }
    }

    @Transactional
    public void removeConnection(Integer userId, Integer connectionId) {
        User user = getUserById(userId, "u");
        User connection = getUserById(connectionId, "u");

        if (user != null && connection != null) {
            user.getConnections().remove(connection);
            userRepository.save(user);

            connection.getConnections().remove(user);
            userRepository.save(connection);
        } else {
            log.warn("Utilisateur ou connection n'existe pas pour être supprimé");
        }
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
