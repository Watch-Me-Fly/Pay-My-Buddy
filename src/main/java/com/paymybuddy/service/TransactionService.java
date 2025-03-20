package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public List<Transaction> getTransactionByUser(String username, String u) {

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {

            return switch (u) {
                case "s" -> transactionRepository.findBySender(user.get());
                case "r" -> transactionRepository.findByReceiver(user.get());
                default -> transactionRepository.findByUser(user.get());
            };
        }
        else {
            throw new RuntimeException("User not found");
        }
    }

    public List<Transaction> getTransactionBetweenTwoUsers(String sender, String receiver) {

        Optional<User> user1 = userRepository.findByUsername(sender);
        Optional<User> user2 = userRepository.findByUsername(receiver);

        if (user1.isPresent() && user2.isPresent()) {
            return transactionRepository.findBySenderAndReceiver(user1.get(), user2.get());
        }
        else
        {
            throw new RuntimeException("Un ou tout les utilisateurs n'ont pas été trouvés");
        }
    }

    public List<Transaction> getTransactionBetween2Users(Integer senderId, Integer receiverId) {

        User sender = getUserById(senderId, "s");
        User receiver = getUserById(receiverId,"r");

        return transactionRepository.findBySenderAndReceiver(sender, receiver);
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