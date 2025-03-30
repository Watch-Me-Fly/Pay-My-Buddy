package com.paymybuddy.controller;

import com.paymybuddy.model.User;
import com.paymybuddy.model.UserProfileDTO;
import com.paymybuddy.repository.UserRepository;
import com.paymybuddy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }
    // create ____________________________________
    // signup a new user
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserProfileDTO user) {
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            return ResponseEntity.badRequest().body("Données invalides");
        }
        userService.signup(
                user.getUsername(),
                user.getEmail(),
                user.getPassword());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Compte utilisateur créé");
    }
    // create a connection link between 2 users
    @PutMapping("/add/{userId}/connections/{connectionId}")
    public ResponseEntity<String> addConnection(
            @PathVariable Integer userId,
            @PathVariable Integer connectionId
    ) {
        // check that both IDs are present and have users
        ResponseEntity<String> validateResponse = validateUsers(userId, connectionId);
        if (validateResponse != null) {
            return validateResponse;
        }
        // if both users are found, create connection
       userService.addConnection(userId, connectionId);
       return ResponseEntity.status(HttpStatus.CREATED)
              .body("Un lien de connexion a été créé");
    }
    // read ______________________________________
    // get user by id
    @GetMapping("/id/{id}")
    public ResponseEntity<User> findById(@PathVariable Integer id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    // get all connections
    @GetMapping("/{id}/connections")
    public ResponseEntity<Set<User>> getAllConnections(@PathVariable Integer id) {
        Set<User> connections = userService.getAllConnections(id);
        if (connections.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(connections);
        }
    }
    // update ____________________________________
    @PutMapping
    public ResponseEntity<String> updateUserInfo(@RequestBody User user) {
        Optional<User> userCheck = userRepository.findById(user.getId());
        if (userCheck.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur n'existe pas");
        } else {
            userService.updateUser(user);
            return ResponseEntity.ok("Données utilisateur mises à jour");
        }
    }
    // delete ____________________________________
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {

        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur n'existe pas");
        } else {
            userService.deleteUser(user.get());
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Utilisateur supprimé");
        }
    }
    @DeleteMapping("/delete/{userId}/connections/{connectionId}")
    public ResponseEntity<String> deleteConnection(
            @PathVariable Integer userId,
            @PathVariable Integer connectionId) {

        // check that both IDs are present and have users
        ResponseEntity<String> validateResponse = validateUsers(userId, connectionId);
        if (validateResponse != null) {
            return validateResponse;
        }
        // check that a connection between both users exist
        if (userService.findConnection(userId, connectionId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Pas de lien entre les 2 utilisateurs trouvé");
        }
        // remove the connection
        userService.removeConnection(userId, connectionId);
        return ResponseEntity.ok("Connexion supprimée");
    }

    private ResponseEntity<String> validateUsers(Integer userId, Integer connectionId) {

        // check : both IDs are present
        if (userId == null || connectionId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Utilisateur ou connection inconnu");
        }
        // check : IDs are not identical, users should not add themselves
        if (userId.equals(connectionId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("L'utilisateur ne peut pas se connecter avec soi-même");
        }

        // search for both users' information
        Optional<User> user = userRepository.findById(userId);
        Optional<User> connection = userRepository.findById(connectionId);

        if (user.isEmpty() || connection.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Un utilisateur n'a pas été retrouvé");
        }
        return null;
    }

}
