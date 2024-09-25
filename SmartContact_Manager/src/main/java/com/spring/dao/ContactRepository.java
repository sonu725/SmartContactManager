package com.spring.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

import com.spring.entities.Contact;
import com.spring.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
    
	// pagination......
	
	
	
	@Query("from Contact c where c.user.id = :userId")
	public Page<Contact> findContactsByUserId(@Param("userId")int userId, Pageable pageable);
	
	public List<Contact> findByNameContainingAndUser(String name, User user);
//	public List<Contact> findByUser(User user);
}
