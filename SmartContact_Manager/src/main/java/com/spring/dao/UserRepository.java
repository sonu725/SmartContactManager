package com.spring.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
		
	@Query("select u from User u where u.email = :email")
	public User getUserByUserName(@RequestParam("email") String email);
}
