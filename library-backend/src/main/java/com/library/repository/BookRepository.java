package com.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.Book;
import com.library.entity.enums.StatusType;

@Repository
public interface BookRepository extends JpaRepository<Book,String>{
	List<Book> findByStatus(StatusType status);
	List<Book> findByTitleContainingOrAuthorContainingOrIsbnContaining(
			String title,String author,String isbn);
}
