package com.jpmc.midascore.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.entity.UserRecord;

@Component
public class Listener {

    @Autowired
    UserRepository userRepo;
    @Autowired
    TransactionRepository transactionRepo;

	@KafkaListener(id = "midas-listener", topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {

        long senderId = transaction.getSenderId();
        long recipientId = transaction.getRecipientId();
        float transactionAmount = transaction.getAmount();

        // Condition 1: The senderId is valid
        if(!userRepo.existsById(senderId)){
            System.out.println("SenderId could not be found. Discarding transaction.");
            return;
        }

        // Condition 2: The recipientId is valid
        if(!userRepo.existsById(recipientId)){
            System.out.println("RecipientId could not be found. Discarding transaction.");
            return;
        }
        
        // Condition 3: The sender has a balance greater than or equal to the transaction amount
        UserRecord sender = userRepo.findById(senderId);
        UserRecord recipient = userRepo.findById(recipientId);

        if(sender.getBalance() < transactionAmount){
            System.out.println("Sender does not have sufficient funds. Discarding transaction.");
            return;
        }

        updateBalances(transaction, sender, recipient);
    }

    private void updateBalances(Transaction transaction, UserRecord sender, UserRecord recipient){

        String url = "http://localhost:8080/incentive";
        RestTemplate restTemplate = new RestTemplate();
        TransactionRecord transactionRecord = new TransactionRecord(sender.getId(), recipient.getId(), transaction.getAmount());
        Incentive incentive = restTemplate.postForObject(url, transactionRecord, Incentive.class);

        float updatedSenderBalance = sender.getBalance() - transaction.getAmount();
        float updatedRecipientBalance = recipient.getBalance() + transaction.getAmount();

        if(incentive != null){
            updatedRecipientBalance += incentive.getAmount();
            transactionRecord.setIncentive(incentive.getAmount());
            transactionRepo.save(transactionRecord);
        }

        sender.setBalance(updatedSenderBalance);
        userRepo.save(sender);
        recipient.setBalance(updatedRecipientBalance);
        userRepo.save(recipient);

        // System.out.println(transactionRecord.toString());

        // System.out.println(sender.getName());
        // System.out.println(sender.getBalance());

        // System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        // System.out.println(recipient.getName());
        // System.out.println(recipient.getBalance());
    }
}
