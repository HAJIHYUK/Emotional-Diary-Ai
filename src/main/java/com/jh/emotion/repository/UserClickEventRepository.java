package com.jh.emotion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.UserClickEvent;

@Repository
public interface UserClickEventRepository extends JpaRepository<UserClickEvent, Long> {



}
