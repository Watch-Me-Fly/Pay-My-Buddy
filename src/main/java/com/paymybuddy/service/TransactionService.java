package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.TransactionDTO;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // general ___________________________________
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

    // create ____________________________________
    public void create(Integer senderId, Integer receiverId, String description, BigDecimal amount) {

        User sender = getUserById(senderId, "s");
        User receiver = getUserById(receiverId,"r");

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
    }

    // read ____________________________________
    public Optional<Transaction> getTransaction(Integer transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public List<TransactionDTO> getTransactionsByUser(int id) {
        // get user and username
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // get user's transactions
        List<Transaction> transactions = transactionRepository.findByUser(user);

        // turn them to DTOs
        List<TransactionDTO> transactionDTOS = new ArrayList<>();

        for (Transaction transaction : transactions) {
            String connectionName = transaction.getSender().equals(user) ?
                    transaction.getReceiver().getUsername() :
                    transaction.getSender().getUsername();

            transactionDTOS.add(new TransactionDTO(
                    transaction.getId(),
                    connectionName,
                    transaction.getDescription(),
                    transaction.getAmount()));
        }
        return transactionDTOS;
    }

    // update __________________________________
    public void update(Transaction transaction) {
        if (transactionRepository.existsById(transaction.getId())) {
            transactionRepository.save(transaction);
        }
        else {
        throw new RuntimeException("La transaction n'existe pas");
        }
    }

    // delete __________________________________
    public void delete(Integer transactionId) {
        Transaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new RuntimeException("La transaction n'existe pas"));

        transactionRepository.delete(transaction);
    }
}