package com.ootd.ootd.service.user;

import com.ootd.ootd.model.dto.user.SignupRequest;
import com.ootd.ootd.model.dto.user.UserDTO;
import com.ootd.ootd.model.entity.User;
import jakarta.validation.constraints.NotBlank;

public interface UserService {
    UserDTO registerUser(SignupRequest signupRequest);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    User findByUsername(@NotBlank(message = "사용자 이름은 필수입니다.") String username);

    UserDTO findByEmail(String email);

}
