package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    public static final String CURRENT_USER_BALANCE = "UserBalance_";
    public User createUser(User user){
        user.setBalance(user.getInitialBalance());
        return userRepository.save(user);
    }
    public BigDecimal getUserBalance(String userId){
        String value = redisTemplate.opsForValue().get(CURRENT_USER_BALANCE+userId);
        if(value != null) return new BigDecimal(value);
        User user = userRepository.findByUserId(userId);
        if(user == null) throw new EntityNotFoundException();
        BigDecimal balance =  user.getBalance();
        return balance;
    }
    public User getUser(String userId){
        User user = userRepository.findByUserId(userId);
        if(user == null) throw new EntityNotFoundException();
        redisTemplate.opsForValue().set(CURRENT_USER_BALANCE+userId, user.getBalance().toString());
        redisTemplate.expire(CURRENT_USER_BALANCE+userId, 10, TimeUnit.MINUTES);
        return user;
    }
    public User save(User user){
        return userRepository.save(user);
    }
    public User updateUserBalance(String userId, BigDecimal amount){
        User user = this.getUser(userId);
        BigDecimal balance = user.getBalance();
        BigDecimal afterBalance = balance.add(amount);
        user.setBalance(afterBalance);
        return this.save(user);
     }
}
