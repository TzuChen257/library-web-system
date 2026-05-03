package com.library.util.security;

import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;

//保持目前登入者
public class LoginUserHolder {
	//建立一個「每個請求執行緒各自獨立保存登入者資料」的容器
	private static final ThreadLocal<LoginUser> HOLDER=new ThreadLocal<>();
	
	//token 驗證成功後，把登入者放進去
	public static void set(LoginUser loginUser) {
		HOLDER.set(loginUser);
	}
	
	// 可登入、可不登入：公開頁面使用
    public static LoginUser getLoginUser() {
        return HOLDER.get();
    }

    // 必須登入：沒有登入就丟 UNAUTHORIZED
    public static LoginUser requireLoginUser() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new LibraryBusinessException(ResponseCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    // 必須是讀者：借閱、預約、申請歸還
    public static void requireReader() {
        LoginUser loginUser = requireLoginUser();
        if (!loginUser.isReader()) {
            throw new LibraryBusinessException(ResponseCode.FORBIDDEN);
        }
    }

    // 必須是管理員：後台管理功能
    public static void requireAdmin() {
        LoginUser loginUser = requireLoginUser();
        if (!loginUser.isAdmin()) {
            throw new LibraryBusinessException(ResponseCode.FORBIDDEN);
        }
    }
    
	//request 結束後清除
	public static void clear() {
		HOLDER.remove();
	}
}
