package com.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long>{
	List<Message> findByReceiver_UserIdOrderByCreatedAtDesc(String userId);//使用者收到的所有訊息，依建立時間新到舊排序
	long countByReceiver_UserIdAndIsReadFalse(String userId);//未讀訊息數
	Optional<Message> findByMessageIdAndReceiver_UserId(Long messageId,String userId);//查某一筆訊息
}