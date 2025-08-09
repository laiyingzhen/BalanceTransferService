package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.TransferService;
import com.example.demo.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class BalanceTransferController {
    @Autowired
    private UserService userService;
    private TransferService transferService;
    @PostMapping("/users")
    public ResponseEntity<ApiResponse> createUser(@RequestBody User request){
        ApiResponse response = new ApiResponse();
        try {
            User savedUser = userService.createUser(request);
            response.setData(savedUser);
            response.setCode(ErrorCode.SUCCESS.getCode());
            response.setMessage(ErrorCode.SUCCESS.getMessage());
        }catch(DataIntegrityViolationException ex){
            response.setCode(ErrorCode.USER_ALREADY_EXIST.getCode());
            response.setCode(ErrorCode.USER_ALREADY_EXIST.getMessage());
        }catch(Exception ex){
            response.setCode(ErrorCode.ERROR.getCode());
            response.setMessage(ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/users/{userId}/balance")
    public ResponseEntity<ApiResponse> getUserBalance(@PathVariable String userId){
        ApiResponse response = new ApiResponse();
        try {
            BigDecimal balance = userService.getUserBalance(userId);
            response.setData(balance);
            response.setCode(ErrorCode.SUCCESS.getCode());
            response.setMessage(ErrorCode.SUCCESS.getMessage());
        }catch(EntityNotFoundException ex){
            response.setCode(ErrorCode.USER_NOT_EXIST.getCode());
            response.setMessage(ErrorCode.USER_NOT_EXIST.getMessage());
        }catch(Exception ex){
            response.setCode(ErrorCode.ERROR.getCode());
            response.setMessage(ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
     @PostMapping("/transfers")
     public ResponseEntity<ApiResponse> transfer(@RequestBody TransferBalanceRequest request){
         ApiResponse response = new ApiResponse();
         try {
             transferService.transferBalance(request.getFromUserId(), request.getToUserId(), request.getAmount());
         }catch(EntityNotFoundException ex) {
             response.setCode(ErrorCode.USER_NOT_EXIST.getCode());
             response.setMessage(ErrorCode.USER_NOT_EXIST.getMessage());
         }catch(IllegalArgumentException ex){
             response.setCode(ErrorCode.TRANSFER_AMOUNT_ERROR.getCode());
             response.setMessage(ErrorCode.TRANSFER_AMOUNT_ERROR.getMessage());
         }catch(Exception ex){
             response.setCode(ErrorCode.ERROR.getCode());
             response.setMessage(ex.getMessage());
         }
         return ResponseEntity.ok(response);
    }
    @GetMapping("/transfers")
    public ResponseEntity<List<TransferHistory>> getTransferHistory(@RequestParam String userId, @RequestParam int pageIndex,@RequestParam int pageSize){
        List<TransferHistory> transferListory = transferService.getTransferHistory(userId, pageIndex, pageSize);
        return ResponseEntity.ok(transferListory);
    }
    @PostMapping("/transfers/{transferId}/cancel")
    public ResponseEntity<ApiResponse> cancelTransfer(@PathVariable String transferId){
        ApiResponse response = new ApiResponse();
        try {
            transferService.cancelTransfer(transferId);
        }catch(UnsupportedOperationException ex){
            response.setCode(ErrorCode.NOT_ALLOW_CANCEL.getCode());
            response.setMessage(ErrorCode.NOT_ALLOW_CANCEL.getMessage());
        }catch(Exception ex){
            response.setCode(ErrorCode.ERROR.getCode());
            response.setMessage(ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
