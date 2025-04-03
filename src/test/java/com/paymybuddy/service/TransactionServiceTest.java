package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.TransactionDTO;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private static UserRepository userRepository;
    @InjectMocks
    private TransactionService transactionService;

    private static User user1;
    private static User user2;
    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeAll
    public static void setUp() {
        user1 = new User();
        user1.setId(1);
        user1.setUsername("Malick");
        user1.setEmail("malick@paymybuddy.com");
        user1.setPassword("MalickPW@");

        user2 = new User();
        user2.setId(2);
        user2.setUsername("Linda");
        user2.setEmail("linda@paymybuddy.com");
        user2.setPassword("LindaPW@");
    }

    @BeforeEach
    public void beforeEach() {
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

    // create ____________________________________
    @DisplayName("Should create a transaction")
    @Test
    public void testCreateTransaction() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));
        when(transactionRepository.save(transaction1)).thenReturn(transaction1);

        transactionService.create(
                user1.getId(),
                user2.getId(),
                transaction1.getDescription(),
                transaction1.getAmount());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // read ______________________________________
    @DisplayName("Should retrieve transactions belonging to a user")
    @Test
    public void testGetTransactionByUser() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user1));
        when(transactionRepository.findByUser(user1)).thenReturn(List.of(transaction1, transaction2));

        List<TransactionDTO> transactions = transactionService.getTransactionsByUser(1);

        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getConnectionName()).isEqualTo(user2.getUsername());
    }

    // update ____________________________________
    @DisplayName("Should update transaction")
    @Test
    public void testUpdateTransaction() {
        transaction1.setDescription("Dinner");

        when(transactionRepository.existsById(transaction1.getId())).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction1);

        transactionService.update(transaction1);

        verify(transactionRepository, times(1)).save(transaction1);
        assertThat(transaction1.getDescription()).isEqualTo("Dinner");
    }

    @DisplayName("Should throw an exception : tries to update a non existing transaction")
    @Test
    public void testUpdateNonExistingTransaction() {

        int nonExistingID = 5;
        Transaction nonExistingTransaction = new Transaction();
        nonExistingTransaction.setId(nonExistingID);

        when(transactionRepository.findById(nonExistingID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.update(nonExistingTransaction))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La transaction n'existe pas");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // delete ____________________________________
    @DisplayName("Should delete a transaction")
    @Test
    public void testDeleteTransaction() {
        int id = transaction1.getId();

        when(transactionRepository
                .findById(id))
                .thenReturn(Optional.of(transaction1));

        transactionService.delete(id);

        verify(transactionRepository, times(1)).delete(transaction1);
    }
    @DisplayName("Should throw an exception : tries to delete a non existing transaction")
    @Test
    public void testDeleteNonExistingTransaction() {
        int nonExistingID = 5;
        when(transactionRepository
                .findById(nonExistingID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.delete(nonExistingID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La transaction n'existe pas");

        verify(transactionRepository, never()).delete(any(Transaction.class));
    }
}
