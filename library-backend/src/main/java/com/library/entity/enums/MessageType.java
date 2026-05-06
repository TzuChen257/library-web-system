package com.library.entity.enums;

public enum MessageType {
	GENERAL,//一般訊息
	BORROW,//借閱通知
	RETURN,//歸還通知
	RESERVATION,//預約通知
	OVERDUE,//逾期通知
    DUE_SOON,//到期前一天提醒
    ACCOUNT//一般公告
}
