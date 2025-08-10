package com.example.demo.service;

import com.example.demo.model.TransferHistory;
import com.example.demo.repository.TransferHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransferServiceTest {
    @Mock
    private TransferHistoryRepository transferHistoryRepository;
    @InjectMocks
    private TransferService transferService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void getTransferHistory() {
        List<TransferHistory> expectedResult = new ArrayList<>();
        TransferHistory expectedHistory = new TransferHistory();
        expectedHistory.setFromUserId("utuser135");
        expectedHistory.setToUserId("utuser998");
        expectedHistory.setTransferId("qerxkdjrwijies");
        expectedHistory.setAmount(new BigDecimal("8108.136"));
        expectedResult.add(expectedHistory);
        when(transferHistoryRepository.findAllByFromUserIdOrToUserId(anyString(),anyString(),any())).thenReturn(expectedResult);
        List<TransferHistory> actuaHistorylList = transferService.getTransferHistory("utuser0020",0,10);
        String actualTransferId = actuaHistorylList.get(0).getTransferId();
        Assertions.assertEquals(1, actuaHistorylList.size());
        Assertions.assertEquals(expectedResult.get(0).getTransferId(),actualTransferId);
    }
}