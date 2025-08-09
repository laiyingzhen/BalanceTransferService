package com.example.demo.repository;

import com.example.demo.model.TransferHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferHistoryRepository extends JpaRepository<TransferHistory,Long> {
    TransferHistory findByTransferId(String transferId);
    List<TransferHistory> findByFromUserIdOrToUserId(String fromUserId, String toUserId, Pageable pageable);
}
