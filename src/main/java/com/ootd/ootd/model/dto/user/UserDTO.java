package com.ootd.ootd.model.dto.user;

import com.ootd.ootd.model.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String name;
    private String phone;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword(); // 응답에 사용하지 않으면 제거
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
    }
}