package com.library.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
}