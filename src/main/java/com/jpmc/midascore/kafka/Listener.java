package com.jpmc.midascore.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.stereotype.Component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.entity.UserRecord;

@Component
public class Listener {

    @Autowired
    UserRepository userRepo;

	@KafkaListener(id = "midas-listener", topics = "${general.kafka-topic}")
    public void listen(Transaction serializedTransaction) {

        long senderId = serializedTransaction.getSenderId();
        long recipientId = serializedTransaction.getRecipientId();
        float transactionAmount = serializedTransaction.getAmount();

        // Condition 1: The senderId is valid
        if(!userRepo.existsById(senderId)){
            System.out.println("SenderId is not valid. Discarding transaction.");
            return;
        }

        // Condition 2: The recipientId is valid
        if(!userRepo.existsById(recipientId)){
            System.out.println("RecipientId is not valid. Discarding transaction.");
            return;
        }
        
        // Condition 3: The sender has a balance greater than or equal to the transaction amount
        UserRecord sender = userRepo.findById(senderId);
        UserRecord recipient = userRepo.findById(recipientId);

        if(sender.getBalance() < serializedTransaction.getAmount()){
            System.out.println("Sender does not have sufficient funds. Discarding transaction.");
            return;
        }

        // Executing transaction and storing it in the DB
        float updatedSenderBalance = sender.getBalance() - transactionAmount;
        sender.setBalance(updatedSenderBalance);
        userRepo.save(sender);

        float updatedRecipientBalance = recipient.getBalance() + transactionAmount;
        recipient.setBalance(updatedRecipientBalance);
        userRepo.save(recipient);

        TransactionRecord savedTransaction = new TransactionRecord(senderId, recipientId, transactionAmount);
        System.out.println(savedTransaction.toString());
    }
}
