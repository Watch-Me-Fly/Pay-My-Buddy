package com.paymybuddy.controller;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionController(TransactionService transactionService, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    // create ____________________________________
    // create a new transaction
    @PostMapping
    public ResponseEntity<String> createTransaction(@RequestBody Transaction transaction) {

        transactionService.create(
                transaction.getSender().getId(),
                transaction.getReceiver().getId(),
                transaction.getDescription(),
                transaction.getAmount());

        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED).body("Trasaction créée");

        return response;
    }

    // read ______________________________________
    @GetMapping("/{id}")
    public Optional<Transaction> getTransaction(@PathVariable int id) {
        return transactionService.getTransaction(id);
    }

    @GetMapping("/between/{user1}/{user2}")
    public List<Transaction> getTransactionsBetween2Users(@PathVariable("user1") String user1, @PathVariable("user2") String user2) {
        return transactionService.getTransactionBetweenTwoUsers(user1, user2);
    }

    @GetMapping("/{username}/{u}")
    public List<Transaction> getTransactionsByUser(@PathVariable String username, @PathVariable String u) {
        return transactionService.getTransactionByUser(username, u);
    }

    // update ____________________________________
    @PutMapping
    public ResponseEntity<String> updateTransaction(@RequestBody Transaction transaction) {
        if (transactionRepository.existsById(transaction.getId())) {
            transactionService.update(transaction);
            return ResponseEntity.status(HttpStatus.OK).body("Transaction mise à jour");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction non trouvée");
        }
    }

    // delete ____________________________________
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable int id) {

        Optional<Transaction> transaction = transactionService.getTransaction(id);

        if (transaction.isPresent()) {
            transactionService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).body("Transaction supprimée");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction non trouvée");
        }

    }

}