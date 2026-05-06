package com.library.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.Reservation;
import com.library.entity.enums.ReservationStatus;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long>{
	
	long countByUser_UserIdAndBook_BookIdAndReservationStatusIn(
            String userId,String bookId,List<ReservationStatus> statuses);

    long countByBook_BookIdAndReservationStatusIn(String bookId,List<ReservationStatus> statuses);

    List<Reservation> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    List<Reservation> findByReservationStatusOrderByCreatedAtDesc(ReservationStatus reservationStatus);
    
    Optional<Reservation> findFirstByBook_BookIdAndReservationStatusOrderByReservationDateAsc(
            String bookId,
            ReservationStatus reservationStatus
    );
    
    Optional<Reservation> findFirstByUser_UserIdAndBook_BookIdAndReservationStatusOrderByReservationDateAsc(
            String userId,
            String bookId,
            ReservationStatus reservationStatus
    );
    
    List<Reservation> findByReservationStatusAndExpireDateBefore(
            ReservationStatus reservationStatus,
            LocalDateTime expireDate
    );
    //管理員統計：依預約狀態計數
    long countByReservationStatus(ReservationStatus reservationStatus);
}
