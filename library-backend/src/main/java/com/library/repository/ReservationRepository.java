package com.library.repository;

import java.util.List;

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
}
