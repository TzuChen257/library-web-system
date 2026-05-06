package com.library.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.util.security.JwtUtil;
import com.library.util.security.LoginUser;
import com.library.util.security.LoginUserHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component//統一檢查 Authorization Header
public class AuthInterceptor implements HandlerInterceptor{
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
		String authHeader=request.getHeader("Authorization");
		if (authHeader == null || authHeader.trim().isEmpty()) {//給有些無需登入的頁面用，後續在service驗證身分
            return true;
        }
		if(!authHeader.startsWith("Bearer ")) {
			throw new LibraryBusinessException(ResponseCode.UNAUTHORIZED,"請先登入");
		}
		String token=authHeader.substring(7);
		if(!jwtUtil.validateToken(token)) {
			throw new LibraryBusinessException(ResponseCode.UNAUTHORIZED,"登入狀態已失效，請重新登入");
		}
		LoginUser loginUser=jwtUtil.parseToken(token);
		LoginUserHolder.set(loginUser);
		return true;
	}
	
	@Override//清除登入狀態
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		LoginUserHolder.clear();
	}
	
}
