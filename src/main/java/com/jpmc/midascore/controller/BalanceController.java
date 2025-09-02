package com.jpmc.midascore.controller;

import com.jpmc.midascore.foundation.Balance;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.entity.UserRecord;

@RestController
public class BalanceController {
	@Autowired
    UserRepository userRepo;

	@GetMapping("/balance")
	public Balance balance(@RequestParam long userId) {
		Optional<UserRecord> user = Optional.ofNullable(userRepo.findById(userId));
		float amount = 0f;
		if(user.isPresent()){
			amount = user.get().getBalance();
		}
		return new Balance(amount);
	}
	
}
