package com.ootd.ootd.repository.cart;

import com.ootd.ootd.model.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {

    @Query("SELECT c FROM Cart c  WHERE c.cartId=:cartId")
    List<Cart> findByCartId(String cartId);

    @Query("SELECT c FROM Cart c WHERE c.cartId = :cartId AND c.productNo = :productNo")
    Optional<Cart> findByCartIdAndProductNo(String cartId, Long productNo);

    @Transactional
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.currentTime <:sevenDaysAgo")
    int deleteByCreatedSevenDays(LocalDateTime sevenDaysAgo);
}
