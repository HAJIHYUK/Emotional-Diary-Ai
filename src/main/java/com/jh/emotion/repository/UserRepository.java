package com.jh.emotion.repository;

import java.util.Optional; // [추가]

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    // kakaoId로 사용자를 조회하는 메소드
    Optional<User> findByKakaoId(Long kakaoId);
}
