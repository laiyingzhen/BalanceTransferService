package com.example.demo.service;

import com.example.demo.model.TransferHistory;
import com.example.demo.model.TransferType;
import com.example.demo.model.User;
import com.example.demo.repository.TransferHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TransferService {
    @Autowired
    private UserService userService;
    @Autowired
    private TransferHistoryRepository transferHistoryRepository;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    public static final String ALLOW_TRANSFER_KEY = "RecentTransferCheck_";

    @Transactional
    public void transferBalance(String fromUserId, String toUserId, BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException();
        User fromUser = userService.updateUserBalance(fromUserId, amount.negate());
        User toUser = userService.updateUserBalance(toUserId, amount);
        LocalDateTime now = LocalDateTime.now();
        String transferId = generateTransferId();
        TransferHistory history = new TransferHistory();
        history.setFromUserId(fromUser.getUserId());
        history.setToUserId(toUser.getUserId());
        history.setAmount(amount);
        history.setTransferTime(now);
        history.setTransferId(transferId);
        history.setTransferType(TransferType.TRANSFER_IN.name());
        transferHistoryRepository.save(history);
        redisTemplate.opsForValue().set(ALLOW_TRANSFER_KEY+transferId, transferId);
        redisTemplate.expire(ALLOW_TRANSFER_KEY, 10, TimeUnit.MINUTES);
    }

    private String generateTransferId(){
        return UUID.randomUUID().toString();
    }
    @Transactional
    public void cancelTransfer(String transferId){
        TransferHistory transferHistory = transferHistoryRepository.findByTransferId(transferId);
        if(transferHistory == null) throw new EntityNotFoundException();
        if(isAllowCancel(transferId)){
            String fromUserId = transferHistory.getFromUserId();
            String toUserId = transferHistory.getToUserId();
            BigDecimal amount = transferHistory.getAmount();

            User fromUser = userService.updateUserBalance(fromUserId, amount);
            User toUser = userService.updateUserBalance(toUserId, amount.negate());
            LocalDateTime now = LocalDateTime.now();
            TransferHistory history = new TransferHistory();
            history.setFromUserId(fromUser.getUserId());
            history.setToUserId(toUser.getUserId());
            history.setAmount(amount);
            history.setTransferTime(now);
            history.setTransferId(generateTransferId());
            history.setTransferType(TransferType.CANCEL.name());
            transferHistoryRepository.save(history);
            redisTemplate.delete(ALLOW_TRANSFER_KEY+transferId);
        }else{
            throw new UnsupportedOperationException();
        }
    }
    private boolean isAllowCancel(String transferId){
        String value = redisTemplate.opsForValue().get(ALLOW_TRANSFER_KEY+transferId);
        if(value == null) return false;
        return true;
    }
    public List<TransferHistory> getTransferHistory(String userId, int pageNumber, int pageSize){
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("transferTime").descending());
        List<TransferHistory> list = transferHistoryRepository.findAllByFromUserIdOrToUserId(userId, userId, pageable);
        return list;
    }
}
