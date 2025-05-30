package com.ootd.ootd.model.entity.brand;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "brand")
@NoArgsConstructor
public class Brand {

    @Id
    @Column(name = "brand_no" , unique = true  )
    private Long brandNo;

    @Column(name = "brand_name" , nullable = false , unique = true)
    private String brandName;

    @Column(name = "brand_Logo_Url")
    private String brandLogoUrl;

    @Column(name = "brand_WebSite" ,length = 500)
    private String brandWebsite;


}
