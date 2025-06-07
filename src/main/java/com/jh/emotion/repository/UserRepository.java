package com.jh.emotion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {


}
