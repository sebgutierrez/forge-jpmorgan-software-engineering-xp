package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
public class TransactionRecord {
	
    @Id
    @GeneratedValue()
    private Long id;

    @Column(nullable = false)
    private Long senderId;

	@Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private float amount;

    protected TransactionRecord() {
    }

    public TransactionRecord(Long senderId, Long recipientId, float amount) {
        this.senderId = senderId;
		this.recipientId = recipientId;
        this.amount = amount;
    }

	public Long getId() {
        return id;
    }

	public Long getSenderId(){
		return senderId;
	}

	public Long getRecipientId(){
		return recipientId;
	}

	public float getAmount(){
		return amount;
	}

	@Override
    public String toString() {
        return String.format("Transaction=[senderId=%d, recipientId=%d, amount=%f]", senderId, recipientId, amount);
    }

}
