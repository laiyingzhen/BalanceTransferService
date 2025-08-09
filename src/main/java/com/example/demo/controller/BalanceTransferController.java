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
    @Autowired
    private TransferService transferService;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
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
             response.setCode(ErrorCode.SUCCESS.getCode());
             response.setMessage(ErrorCode.SUCCESS.getMessage());
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
    public ResponseEntity<ApiResponse> getTransferHistory(@RequestParam String userId, @RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize){
        ApiResponse response = new ApiResponse();
        if(pageNumber == null || pageSize == null){
            pageNumber = DEFAULT_PAGE_NUMBER;
            pageSize = DEFAULT_PAGE_SIZE;
        }
        try {
            List<TransferHistory> transferListory = transferService.getTransferHistory(userId, pageNumber, pageSize);
            response.setData(transferListory);
            response.setCode(ErrorCode.SUCCESS.getCode());
            response.setMessage(ErrorCode.SUCCESS.getMessage());
        }catch(Exception ex){
            response.setCode(ErrorCode.ERROR.getCode());
            response.setMessage(ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    @PostMapping("/transfers/{transferId}/cancel")
    public ResponseEntity<ApiResponse> cancelTransfer(@PathVariable String transferId){
        ApiResponse response = new ApiResponse();
        try {
            transferService.cancelTransfer(transferId);
            response.setCode(ErrorCode.SUCCESS.getCode());
            response.setMessage(ErrorCode.SUCCESS.getMessage());
        }catch(EntityNotFoundException ex){
            response.setCode(ErrorCode.TRANSFER_ID_ERROR.getCode());
            response.setMessage(ErrorCode.TRANSFER_ID_ERROR.getMessage());
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
