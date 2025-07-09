package com.ootd.ootd.repository.user;

import com.ootd.ootd.model.entity.order.UserOrder;
import com.ootd.ootd.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<UserOrder> findTop5ByOrderByCreatedAtDesc();

}
