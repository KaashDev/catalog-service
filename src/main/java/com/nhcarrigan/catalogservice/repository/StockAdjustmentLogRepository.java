package com.nhcarrigan.catalogservice.repository;

import com.nhcarrigan.catalogservice.entity.StockAdjustmentLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAdjustmentLogRepository extends JpaRepository<StockAdjustmentLog, Long> {

  List<StockAdjustmentLog> findByProductIdOrderByTimestampDesc(Long productId);
}
