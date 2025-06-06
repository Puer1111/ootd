package com.ootd.ootd.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    @NotBlank(message = "사용자 이름은 필수 항목입니다")
    @Size(min = 2, max = 20, message = "사용자 이름은 2~20자 사이여야 합니다")
    private String username;

    @NotBlank(message = "이메일은 필수 항목입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "이름은 필수 항목입니다")
    private String name;

    @NotBlank(message = "전화번호는 필수 항목입니다")  // 👈 추가
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")  // 👈 추가
    private String phone;  // 👈 추가

    @NotBlank(message = "비밀번호는 필수 항목입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;
}