package com.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setUsername("Malick");
        user1.setEmail("malick@paymybuddy.com");
        user1.setPassword("MalickPW@");
        userRepository.save(user1);

        user2 = new User();
        user2.setUsername("Linda");
        user2.setEmail("linda@paymybuddy.com");
        user2.setPassword("LindaPW@");
        userRepository.save(user2);

        transaction1 = new Transaction();
        transaction1.setSender(user1);
        transaction1.setReceiver(user2);
        transaction1.setAmount(BigDecimal.valueOf(100.0));
        transactionRepository.save(transaction1);

        transaction2 = new Transaction();
        transaction2.setSender(user2);
        transaction2.setReceiver(user1);
        transaction2.setAmount(BigDecimal.valueOf(250.0));
        transactionRepository.save(transaction2);
    }

    @DisplayName("Find a transaction by any user (user1 or user2)")
    @Test
    public void testFindByUser() {
        // Given
        // user1 was the sender in transaction #1 + the receiver in transaction #2 = 2 transactions
        // When
        List<Transaction> transactions = transactionRepository.findByUser(user1);
        // Then
        assertThat(transactions).hasSize(2);
    }

    @DisplayName("Find a transaction made as sender only")
    @Test
    public void testFindBySender() {
        // When
        List<Transaction> transactions = transactionRepository.findBySender(user1);
        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @DisplayName("Find a transaction as receiever only")
    @Test
    public void testFindByReceiver() {
        // When
        List<Transaction> transactions = transactionRepository.findByReceiver(user1);
        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(250.0));
    }

    @DisplayName("Find a transaction between 2 given people in a given direction")
    @Test
    public void testFindByReceiverAndSender() {
        // Given
        // user1 was the sender in transaction #1, and user2 is the receiver
        // When
        List<Transaction> transactions = transactionRepository.findBySenderAndReceiver(user1, user2);
        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

}
