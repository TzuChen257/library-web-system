package com.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.User;
import com.library.entity.enums.StatusType;

@Repository
public interface UserRepository extends JpaRepository<User,String>{
	Optional<User> findByUsername(String username);
	//管理員查讀者
	boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndUserIdNot(String email, String userId);
    List<User> findByStatus(StatusType status);
    List<User> findByRole(String role);
    List<User> findByUsernameContainingOrNameContainingOrEmailContaining(
            String username,
            String name,
            String email
    );
}
