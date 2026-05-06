package com.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.Book;
import com.library.entity.enums.StatusType;

@Repository
public interface BookRepository extends JpaRepository<Book,String>{
	List<Book> findByStatus(StatusType status);
	List<Book> findByTitleContainingOrAuthorContainingOrIsbnContaining(
			String title,String author,String isbn);
	// 首頁公開統計：上架書目數
    long countByStatus(StatusType status);
    //批次上傳使用
    Optional<Book> findByIsbn(String isbn);
}
