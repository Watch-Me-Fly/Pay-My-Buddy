package com.paymybuddy.controller;

import com.paymybuddy.model.User;
import com.paymybuddy.model.UserProfileDTO;
import com.paymybuddy.repository.UserRepository;
import com.paymybuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
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
    public ResponseEntity<Set<UserProfileDTO>> getAllConnections(@PathVariable Integer id) {

        log.info("getAllConnections id: {}", id);
        Set<UserProfileDTO> connections = userService.getAllConnections(id);

        if (connections.isEmpty()) {
            log.debug("connections list is empty");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            log.info("connections list contains {} connections", connections.size());
            return ResponseEntity.ok(connections);
        }
    }
    // get a specific connection when authenticated
    @GetMapping("/connection/{email}")
    public ResponseEntity<UserProfileDTO> findConnectionByEmail(HttpSession session, @PathVariable String email) {
        log.info("find connection by email: {}", email);
        // find connected user by session email
        if (session.getAttribute("email") == null) {
            log.error("user is null");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String sessionEmail = session.getAttribute("email").toString();
        Optional<User> connectedUser = userService.findByEmail(sessionEmail);
        if (connectedUser.isEmpty()) {
            log.error("user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // search in this user's connections
        int id = connectedUser.get().getId();
        Optional<User> connection = userService.findConnection(id, email);
        if (connection.isEmpty()) {
            log.error("no connection found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // return results as DTO
        UserProfileDTO foundConnection = new UserProfileDTO(
                connection.get().getUsername(),
                connection.get().getEmail(),
                null);
        return ResponseEntity.ok(foundConnection);
    }
    // get the user's data by email from the session
    @GetMapping("/email")
    public ResponseEntity<User> findByEmailFromSession(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            log.debug("email is empty");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // todo
        }
        Optional<User> connectedUser = userService.findByEmail(email);
        if (connectedUser.isEmpty()) {
            log.debug("user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(connectedUser.get());
    }
    @GetMapping("/find/{email}")
    public ResponseEntity<User> findByEmail(@PathVariable String email) {
        Optional<User> find = userService.findByEmail(email);
        return find.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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
        // find connection's email
        Optional<User> connection = userRepository.findById(connectionId);
        if (connection.isPresent()) {
            String email = connection.get().getEmail();
            // check that a connection between both users exist
            if (userService.findConnection(userId, email).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Pas de lien entre les 2 utilisateurs trouvé");
            }
            // remove the connection
            userService.removeConnection(userId, connectionId);
        }
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
