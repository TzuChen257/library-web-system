package com.library.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.library.entity.BorrowRecord;
import com.library.entity.enums.BorrowStatus;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord,Long>{
	//確認借閱上限
	long countByUser_UserIdAndBorrowStatusIn(String userId,List<BorrowStatus> statuses);
	//我的借閱，查某使用者目前借閱中/歸還待審核紀錄
	List<BorrowRecord> findByUser_UserIdAndBorrowStatusInOrderByBorrowDateDesc(
			String userId,List<BorrowStatus> statuses);
	//我的借閱，查某使用者全部借閱歷史
	List<BorrowRecord> findByUser_UserIdOrderByBorrowDateDesc(String userId);
	//管理員：查某狀態的書籍(例如未審核)
	List<BorrowRecord> findByBorrowStatusOrderByCreatedAtDesc(BorrowStatus borrowStatus);
	//管理員：查找所有借閱紀錄
	List<BorrowRecord> findAllByOrderByCreatedAtDesc();
	//逾期處理
	List<BorrowRecord> findByBorrowStatusAndDueDateAndDueSoonNoticeSentAtIsNull(
            BorrowStatus borrowStatus,LocalDate dueDate);
    List<BorrowRecord> findByBorrowStatusAndDueDateAndOverdueNoticeSentAtIsNull(
            BorrowStatus borrowStatus,LocalDate dueDate);
    List<BorrowRecord> findByBorrowStatusAndDueDateLessThanEqualAndOverdue7NoticeSentAtIsNull(
            BorrowStatus borrowStatus,LocalDate dueDate);
    //首頁公開統計：今日借閱數
    long countByBorrowDate(LocalDate borrowDate);
    //首頁公開統計：本月借閱數
    long countByBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    //首頁公開統計：本月熱門借閱 Top N
    @Query("""
           SELECT b.bookCopy.book.bookId,
                  b.bookCopy.book.title,
                  b.bookCopy.book.author,
                  COUNT(b.borrowId)
           FROM BorrowRecord b
           WHERE b.borrowDate BETWEEN :startDate AND :endDate
           GROUP BY b.bookCopy.book.bookId,
                    b.bookCopy.book.title,
                    b.bookCopy.book.author
           ORDER BY COUNT(b.borrowId) DESC
           """)
    List<Object[]> findTopBorrowedBooks(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    //管理員首頁統計：依借閱狀態計數
    long countByBorrowStatus(BorrowStatus borrowStatus);
    //管理員報表：年度每月借閱數
    //row[0]=月份/row[1]=借閱數
    @Query("""
           SELECT MONTH(b.borrowDate),
                  COUNT(b.borrowId)
           FROM BorrowRecord b
           WHERE YEAR(b.borrowDate) = :year
           GROUP BY MONTH(b.borrowDate)
           ORDER BY MONTH(b.borrowDate)
           """)
    List<Object[]> countMonthlyBorrowsByYear(@Param("year") int year);
    //管理員報表：年度熱門書籍Top N
    //row[0]=bookId/[1]=title/[2]=author/[3]=borrowCount
    @Query("""
           SELECT b.bookCopy.book.bookId,
                  b.bookCopy.book.title,
                  b.bookCopy.book.author,
                  COUNT(b.borrowId)
           FROM BorrowRecord b
           WHERE YEAR(b.borrowDate) = :year
           GROUP BY b.bookCopy.book.bookId,
                    b.bookCopy.book.title,
                    b.bookCopy.book.author
           ORDER BY COUNT(b.borrowId) DESC
           """)
    List<Object[]> findTopBorrowedBooksByYear(
            @Param("year") int year,
            Pageable pageable
    );
    //管理員報表：年度讀者借閱排行 Top N
    //row[0]=userId/[1]=username/[2]=name/[3]=borrowCount
    @Query("""
           SELECT b.user.userId,
                  b.user.username,
                  b.user.name,
                  COUNT(b.borrowId)
           FROM BorrowRecord b
           WHERE YEAR(b.borrowDate) = :year
           GROUP BY b.user.userId,
                    b.user.username,
                    b.user.name
           ORDER BY COUNT(b.borrowId) DESC
           """)
    List<Object[]> findTopReadersByYear(
            @Param("year") int year,
            Pageable pageable
    );
}