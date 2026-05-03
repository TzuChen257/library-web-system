package com.library.service;

import java.util.List;

import com.library.dto.borrow.BorrowResponse;
import com.library.entity.enums.BorrowStatus;

public interface BorrowService {
	//讀者
	BorrowResponse borrowBook(String bookId);
    List<BorrowResponse> getMyCurrentBorrows();
    void requestReturn(Long borrowId);
    //管理員
    List<BorrowResponse> getAdminBorrows(BorrowStatus borrowStatus);
    List<BorrowResponse> getReturnPendingBorrows();
    BorrowResponse approveReturn(Long borrowId, BorrowStatus resultStatus);
}
