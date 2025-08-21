package com.jh.emotion.repository;

import java.time.LocalDateTime;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.jh.emotion.entity.UserClickEvent;

@Repository
public interface UserClickEventRepository extends JpaRepository<UserClickEvent, Long> {

    // 유저 아이디로 클릭 이벤트 조회
    List<UserClickEvent> findByUser_UserId(Long userId);

    //현재 날짜 기준 최근 1달 조회
    List<UserClickEvent> findByUser_UserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    //유저 아이디와 생성일자 기준 최근 1달 조회 (중복되는 genre 카운트해서 조회 ex. 최소 5번 이상 genre 조회) , 최근 1달간의 클릭 조회후 유저 선호 장르 추가하기 위해 사용
    @Query("SELECT MIN(u.type), u.genre, COUNT(u) FROM UserClickEvent u WHERE u.user.userId = :userId AND u.createdAt > :after GROUP BY u.genre HAVING COUNT(u) >= :minCount")
    List<Object[]> findTypeGenreCountsByUserAndCreatedAtAfter(
        @Param("userId") Long userId,
        @Param("after") LocalDateTime after,
        @Param("minCount") long minCount
    );
}
