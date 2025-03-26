package com.paymybuddy.controller.pages;

import com.paymybuddy.model.User;
import com.paymybuddy.model.UserProfileDTO;
import com.paymybuddy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Log4j2
public class ProfileRestController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserInfo(Principal principal) {
        log.info("Received GET request for profile");
        log.info("Principal: {}", principal);

        ResponseEntity<?> response = findUser(principal);

        if (!(response.getBody() instanceof User userFound)) {
            return ResponseEntity.status(response.getStatusCode())
                    .build();
        }

        UserProfileDTO userDTO = new UserProfileDTO(
                userFound.getUsername(),
                userFound.getEmail(),
                null);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody User user, Principal principal) {
        log.info("Received POST request for profile");

        ResponseEntity<?> response = findUser(principal);

        if (!(response.getBody() instanceof User userFound)) {
            return ResponseEntity.status(response.getStatusCode())
                    .body(response.toString());
        }

        // update password
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            userService.updatePassword(userFound.getId(), user.getPassword());
        }
        // update email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            userFound.setEmail(user.getEmail());
            userService.updateUser(userFound);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body("Informations mises à jour");
    }

    @DeleteMapping("/profile")
    public ResponseEntity<String> deleteProfile(Principal principal) {

        log.info("Received DELETE request for profile");
        ResponseEntity<?> response = findUser(principal);

        if (!(response.getBody() instanceof User userFound)) {
            return ResponseEntity.status(response.getStatusCode())
                    .body(response.toString());
        }

        userService.deleteUser(userFound);
        return ResponseEntity.ok("Compte supprimé avec succès");
    }

    private ResponseEntity<?> findUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Utilisateur non authentifié");
        }

        Optional<User> searchUser = userService.findByEmail(principal.getName());
        if (searchUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé");
        }

        return ResponseEntity.ok(searchUser.get());
    }

}