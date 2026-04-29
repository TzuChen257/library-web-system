package com.library.enums;

public enum BookCopyStatus {
	AVAILABLE,//可借閱
	BORROWED,//借出中
	RETURN_PENDING,//歸還待審核
	RESERVED,//已保留
	DAMAGED,//毀損
	LOST,//遺失
	REMOVED//其他原因移除
}
