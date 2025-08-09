package com.example.demo.service;

import com.example.demo.model.TransferHistory;
import com.example.demo.model.TransferType;
import com.example.demo.model.User;
import com.example.demo.repository.TransferHistoryRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
        User fromUser = userService.getUser(fromUserId);
        User toUser = userService.getUser(toUserId);
        BigDecimal fromUserAfterBalance = fromUser.getBalance().subtract(amount);
        BigDecimal toUserAfterBalance = toUser.getBalance().add(amount);
        fromUser.setBalance(fromUserAfterBalance);
        toUser.setBalance(toUserAfterBalance);
        userService.save(fromUser);
        userService.save(toUser);
        LocalDateTime now = LocalDateTime.now();
        String transferId = generateTransferId(now);
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
    private String generateTransferId(LocalDateTime dateTime){
        StringBuffer sb = new StringBuffer();
        sb.append(dateTime.toString()).append(UUID.randomUUID());
        return sb.toString();
    }
    @Transactional
    public void cancelTransfer(String transferId){
        TransferHistory transferHistory = transferHistoryRepository.findByTransferId(transferId);
        if(isAllowCancel(transferId)){
            String fromUserId = transferHistory.getFromUserId();
            String toUserId = transferHistory.getToUserId();
            BigDecimal amount = transferHistory.getAmount();

            User fromUser = userService.getUser(fromUserId);
            BigDecimal fromUserBalance = fromUser.getBalance();
            User toUser = userService.getUser(toUserId);
            BigDecimal toUserBalance = toUser.getBalance();
            BigDecimal fromUserAfterBalance = fromUserBalance.add(amount);
            BigDecimal toUserAfterBalance = toUserBalance.subtract(amount);
            fromUser.setBalance(fromUserAfterBalance);
            toUser.setBalance(toUserAfterBalance);
            userService.save(fromUser);
            userService.save(toUser);
            LocalDateTime now = LocalDateTime.now();
            TransferHistory history = new TransferHistory();
            history.setFromUserId(fromUser.getUserId());
            history.setToUserId(toUser.getUserId());
            history.setAmount(amount);
            history.setTransferTime(now);
            history.setTransferId(generateTransferId(now));
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
    public List<TransferHistory> getTransferHistory(String userId, int pageIndex, int pageSize){
        Pageable sortedPageable = PageRequest.of(pageIndex, pageSize, Sort.by("transferTime").descending());
        return transferHistoryRepository.findByFromUserIdOrToUserId(userId, userId, sortedPageable);
    }
}
