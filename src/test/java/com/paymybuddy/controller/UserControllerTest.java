package com.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.model.User;
import com.paymybuddy.model.UserProfileDTO;
import com.paymybuddy.repository.UserRepository;
import com.paymybuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private UserService mockUserService;
    @InjectMocks
    private UserController userController;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();

        user1 = new User();
        user1.setId(1);
        user1.setUsername("Malick");
        user1.setPassword("MalickPW");
        user1.setEmail("Malick@mail.fr");

        user2 = new User();
        user2.setId(2);
        user2.setUsername("Linda");
        user2.setEmail("Linda@example.com");
        user2.setPassword("LindaPW@@");
    }

    @DisplayName("signup a new user")
    @Test
    public void testCreateUser() throws Exception {
        doNothing().when(mockUserService)
                        .signup(anyString(), anyString(), anyString());

        mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Compte utilisateur créé"));

        verify(mockUserService, times(1))
                .signup(user1.getUsername(), user1.getEmail(), user1.getPassword());
    }

    @DisplayName("create a connection link between 2 users")
    @Test
    public void testAddConnection() throws Exception {
        doReturn(Optional.of(user1))
                .when(mockUserRepository).findById(user1.getId());
        doReturn(Optional.of(user2))
                .when(mockUserRepository).findById(user2.getId());

        doNothing().when(mockUserService)
                .addConnection(user1.getId(), user2.getId());

        mockMvc.perform(
                put("/users/add/{userId}/connections/{connectionId}", user1.getId(), user2.getId()))
                .andExpect(status().isCreated())
                .andExpect(content().string("Un lien de connexion a été créé"));

        verify(mockUserService, times(1))
                .addConnection(user1.getId(), user2.getId());
    }

    @DisplayName("get user by id")
    @Test
    public void testGetUserById() throws Exception {
        String expectedName = "Malick";
        String expectedEmail = "Malick@mail.fr";

        when(mockUserService.findById(user1.getId())).thenReturn(Optional.of(user1));

        mockMvc.perform(
                get("/users/id/{id}", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(expectedName))
                .andExpect(jsonPath("$.email").value(expectedEmail));

        verify(mockUserService, times(1)).findById(user1.getId());
    }

    @DisplayName("get all connections")
    @Test
    public void testGetAllConnections() throws Exception {
        int id = user1.getId();
        Set<UserProfileDTO> connections = new HashSet<>();
        connections.add(new UserProfileDTO(user1.getUsername(), user1.getEmail(), user1.getPassword()));
        connections.add(new UserProfileDTO(user2.getUsername(), user2.getEmail(), user2.getPassword()));

        when(mockUserService.getAllConnections(id)).thenReturn(connections);

        mockMvc.perform(get("/users/{id}/connections", id))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper
                        .writeValueAsString(connections)));

        verify(mockUserService, times(1)).getAllConnections(id);
    }

    @DisplayName("update user information")
    @Test
    public void testUpdateUser() throws Exception {
        int id = user1.getId();
        when(mockUserRepository.findById(id)).thenReturn(Optional.of(user1));
        doNothing().when(mockUserService).updateUser(any(User.class));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(content().string("Données utilisateur mises à jour"));

        // capture the actual object passed to the service to avoid the error (arguments are different)
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserService, times(1)).updateUser(userCaptor.capture());

        // verify this user object has the same data
        User capturedUser = userCaptor.getValue();
        assertEquals(user1.getUsername(), capturedUser.getUsername());
        assertEquals(user1.getEmail(), capturedUser.getEmail());
    }

    @DisplayName("delete user")
    @Test
    public void testDeleteUser() throws Exception {
        String username = user1.getUsername();
        when(mockUserService.findByUsername(username)).thenReturn(Optional.of(user1));
        doNothing().when(mockUserService).deleteUser(any(User.class));

        mockMvc.perform(delete("/users/{username}", username))
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur supprimé"));

        verify(mockUserService, times(1)).deleteUser(any(User.class));
    }

    @DisplayName("delete connection")
    @Test
    public void testDeleteConnection() throws Exception {
        // mock users
        int userId = user1.getId();
        String connectionEmail = user2.getEmail();
        int connectionId = user2.getId();

        when(mockUserRepository.findById(userId))
                .thenReturn(Optional.of(user1));
        when(mockUserRepository.findById(connectionId))
                .thenReturn(Optional.of(user2));

        // mock a connection
        Set<User> connections = new HashSet<>();
        connections.add(user1);
        connections.add(user2);

        when(mockUserRepository.findConnectionsByUserId(userId))
                .thenReturn(connections);
        when(mockUserService.findConnection(userId, connectionEmail))
                .thenReturn(Optional.of(user2));

        // act
        doNothing().when(mockUserService).removeConnection(anyInt(), anyInt());

        mockMvc.perform(delete("/users/delete/{userId}/connections/{connectionId}", userId, connectionId))
                .andExpect(status().isOk())
                .andExpect(content().string("Connexion supprimée"));

        verify(mockUserService, times(1)).removeConnection(userId, connectionId);
    }

}
