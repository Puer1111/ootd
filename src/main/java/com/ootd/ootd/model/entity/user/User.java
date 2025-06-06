package com.ootd.ootd.model.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor // 매개변수가 없는 기본 생성자를 자동 생성
@AllArgsConstructor // 모든 필드를 매개변수로 받는 생성자를 자동 생성
@Builder
public class User {

    //Entry는 JPA에서 DB하고 테이블이 매핑되는 엔티티 클래스임을 알려주는 어노테이션임
    // 난 USER 클래스를 JAP가 관리함으로서 DB의 users 와 매핑되어 DB를 바로 연동해주는거임
    // 예시로 설명
    // 이 코드는 결국 users라는 테이블에 다음과 같은 컬럼을 생성하게 됩니다:
    //
    // 컬럼명	타입	제약조건
    // id	Long	기본키, 자동 증가
    // email	String	NotBlank, 이메일 형식, 유일
    // password	String	NotBlank
    // gender	String	NotBlank
    // age	Integer	NotNull, 최소 1
    // phone	String	NotBlank

    @Id // Primary Key임 JPA에서 엔티티는 반드시 @Id로 지정된 기본키가 필요함
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키값을 자동으로 생성하는 방식임
    private long id; // 위 ID와 GeneratedValue와 함꼐 id 필드는 엔티티의 Primary Key가 되서 DB에 자동으로 값을 증가시켜 저장함

    @Column(nullable = false, unique = true) // 컬럼의 제약조건을 설정 = null값을 허용치않고 값이 유일해야함(중복저장불가)
    private String username; // 반드시 입력해야하고 중복된 username을 허락치않는 username의 필드가 완성됌.

    @Column(nullable = false) // 그럼 애는 비번이 비지않아야한다는 뜻임.
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String name; // 이름은 겹쳐도 오케이

    @Column(name = "created_at") // DB 컬럼이름 지정한거 DB에서는 created_at으로 저장해야함
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String phone;

    @PrePersist // 엔티티가 처음 저장되기 직전에 자동으로 호출됨
    protected void onCreate() { // insert전에 실행된다는데 좀 모르겠음 찾아봐야함 아마 시간갱신 코드같음
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate // 애는 DB에 업데이트되기 직전에 호출 현재시간으로 갱신함
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 새로운 필드: 사용자 역할 추가
    @ElementCollection(fetch = FetchType.EAGER) // 여러 개의 역할을 저장하기 위해 List<String>을 사용
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // user_roles 테이블에 저장됨
    @Column(name = "role") // 역할 컬럼 지정
    private List<String> roles; // 사용자의 역할을 저장할 필드, 예: ["ROLE_USER", "ROLE_ADMIN"]

//    // 역할을 반환하는 메소드 추가 (로그인 시 역할을 처리하기 위해 필요)
//    public List<String> getRoles() {
//        return roles;
//    }
}
