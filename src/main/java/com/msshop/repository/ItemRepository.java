package com.msshop.repository;

import com.msshop.domain.Item;
import com.msshop.domain.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    List<Item> findAllByStatusOrderByCreatedAtDesc(ItemStatus status);

    List<Item> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query("DELETE FROM Item i WHERE i.status = :status")
    int deleteAllByStatus(ItemStatus status);

    int countByStatus(ItemStatus status);

    @Query("SELECT DISTINCT i.warehouseChar FROM Item i WHERE i.warehouseChar IS NOT NULL AND i.warehouseChar <> '' ORDER BY i.warehouseChar")
    List<String> findDistinctWarehouseChars();

    @Query("SELECT DISTINCT i.sellerName FROM Item i WHERE i.sellerName IS NOT NULL AND i.sellerName <> '' ORDER BY i.sellerName")
    List<String> findDistinctSellerNames();
}
