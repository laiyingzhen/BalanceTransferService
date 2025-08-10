package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private UserService userService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn( null);
    }
    @Test
    void testGetUserBalance() {
        User expectedUser = new User("ut_user0810", new BigDecimal("1000"), new BigDecimal("5368"));
        when(userRepository.findByUserId(Mockito.anyString())).thenReturn(expectedUser);

        BigDecimal actualBalance = userService.getUserBalance("ut_user0810");
        assertEquals(expectedUser.getBalance(), actualBalance);
    }
    @Test
    void testGetUser(){
        when(userRepository.findByUserId(Mockito.anyString())).thenReturn(null);
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getUser(Mockito.anyString()));
    }
}
