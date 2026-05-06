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
	//管理員
	boolean existsByCopyCode(String copyCode);//新增館藏時檢查條碼是否重複
    boolean existsByCopyCodeAndCopyIdNot(String copyCode, String copyId);//修改館藏時檢查條碼是否被其他館藏使用
    List<BookCopy> findByBook_BookId(String bookId);//查某一本書底下的所有館藏
    List<BookCopy> findByCopyStatus(BookCopyStatus copyStatus);//管理員依館藏狀態查詢
    List<BookCopy> findByBook_BookIdAndCopyStatus(String bookId,BookCopyStatus copyStatus);//依書目 + 館藏狀態查詢
    List<BookCopy> findByCopyCodeContaining(String keyword);//用條碼搜尋館藏
    //首頁公開統計：全館可借館藏數
    long countByCopyStatus(BookCopyStatus copyStatus);
}
