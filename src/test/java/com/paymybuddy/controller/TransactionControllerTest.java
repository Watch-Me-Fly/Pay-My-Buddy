package com.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.TransactionDTO;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.service.TransactionService;
import com.paymybuddy.service.UserService;
import org.junit.jupiter.api.BeforeAll;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class TransactionControllerTest {

    private static MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TransactionService mockTransactionService;
    @Mock
    private TransactionRepository mockTransactionRepository;
    @InjectMocks
    private TransactionController transactionController;

    private Transaction transaction1;
    private Transaction transaction2;
    private static User user1;
    private static User user2;

    @BeforeAll
    static void setUp() {
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

    @BeforeEach
    void setUpTransaction() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .build();
        objectMapper = new ObjectMapper();

        transaction1 = new Transaction();
        transaction1.setId(1);
        transaction1.setSender(user1);
        transaction1.setReceiver(user2);
        transaction1.setAmount(BigDecimal.valueOf(100.0));

        transaction2 = new Transaction();
        transaction2.setId(2);
        transaction2.setSender(user2);
        transaction2.setReceiver(user1);
        transaction2.setAmount(BigDecimal.valueOf(250.0));
    }

    @DisplayName("create a new transaction")
    @Test
    void testCreateNewTransaction() throws Exception {
        int senderId = user1.getId();
        int receiverId = user2.getId();
        String description = transaction1.getDescription();
        BigDecimal amount = transaction1.getAmount();

        doNothing().when(mockTransactionService)
                .create(senderId, receiverId, description, amount);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction1)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Trasaction créée"));

        verify(mockTransactionService, times(1)).create(senderId, receiverId, description, amount);

    }

    @DisplayName("get a transaction")
    @Test
    void testGetTransaction() throws Exception {
        int senderId = user1.getId();
        int transactionId = transaction1.getId();

        when(mockTransactionService.getTransaction(senderId))
                .thenReturn(Optional.of(transaction1));
        when(mockTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction1));

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(content().json(
                objectMapper.writeValueAsString(transaction1)));

        verify(mockTransactionService, times(1)).getTransaction(senderId);
    }

    @DisplayName("get transactions for a given user")
    @Test
    void testGetTransactionsByUser() throws Exception {
        TransactionDTO transactionDTO1 = new TransactionDTO(1, user2.getUsername(), null, BigDecimal.valueOf(100.0));
        TransactionDTO transactionDTO2 = new TransactionDTO(2, user1.getUsername(), null, BigDecimal.valueOf(250.0));

        List<TransactionDTO> transactionsDTO = List.of(transactionDTO1, transactionDTO2);

        when(mockTransactionService.getTransactionsByUser(anyInt())).thenReturn(transactionsDTO);

        mockMvc.perform(get("/transactions/user/{userId}", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(transactionsDTO)));

        verify(mockTransactionService, times(1)).getTransactionsByUser(user1.getId());
    }

    @DisplayName("update details of a transaction")
    @Test
    void testUpdateTransaction() throws Exception {
        int id = transaction1.getId();

        when(mockTransactionRepository.existsById(id))
                .thenReturn(true);
        doNothing().when(mockTransactionService)
                .update(any(Transaction.class));

        mockMvc.perform(put("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction1)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction mise à jour"));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(mockTransactionService, times(1))
                .update(captor.capture());

        Transaction capturedValues = captor.getValue();
        assertEquals(transaction1.getSender().getId(), capturedValues.getSender().getId());
        assertEquals(transaction1.getReceiver().getId(), capturedValues.getReceiver().getId());
    }

    @DisplayName("delete a transaction")
    @Test
    void testDeleteTransaction() throws Exception {
        int id = transaction1.getId();

        when(mockTransactionService.getTransaction(id))
                .thenReturn(Optional.of(transaction1));
        doNothing().when(mockTransactionService).delete(anyInt());

        mockMvc.perform(delete("/transactions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction supprimée"));

        verify(mockTransactionService, times(1)).delete(id);
    }
}
