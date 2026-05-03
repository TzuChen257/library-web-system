package com.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.BookCopy;
import com.library.entity.enums.BookCopyStatus;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy,String>{
	long countByBook_BookId(String bookId);//計算總館藏數
	long countByBook_BookIdAndCopyStatus(String bookId,BookCopyStatus copyStatus);//計算可借數
	Optional<BookCopy> findFirstByBook_BookIdAndCopyStatusOrderByCopyIdAsc(
			String bookId,BookCopyStatus copyStatus);//借書時找第一本可借館藏
	List<BookCopy> findByBook_BookIdOrderByCopyIdAsc(String bookId);//書籍詳情頁顯示館藏清單
}
