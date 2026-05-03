package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.BookCategory;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategory,String>{

}
