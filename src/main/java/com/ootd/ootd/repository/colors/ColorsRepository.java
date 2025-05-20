package com.ootd.ootd.repository.colors;

import com.ootd.ootd.model.entity.colors.Colors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorsRepository extends JpaRepository<Colors, Long> {
}
