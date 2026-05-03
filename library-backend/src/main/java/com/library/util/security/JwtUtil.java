package com.library.util.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

//產生與解析 token
@Component
public class JwtUtil {
	// HS256 至少需要 32 bytes 的密鑰
	private static final String SECRET = "IreadLibrarySecretKeyForJwtToken8451d2a1s2dasdds54d4a";
	private static final long EXPIRE_TIME = 24*60*60*1000L;//先設一天可以再加
	//加密轉換為SecretKey物件後續使用
	private SecretKey getSigninKey() {
		return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
	}
	//產生token
	public String generateToken(String userId,String username,String role) {
		return Jwts.builder()
				.subject(userId)//Token的核心主體(唯一)
				.claim("role", role)//使用者權限
				.claim("username", username)//使用者名稱
				.issuedAt(new Date())//現在簽發
				.expiration(new Date(System.currentTimeMillis()+EXPIRE_TIME))
				.signWith(getSigninKey())//私鑰
				.compact();
	}
	//解析token
	public LoginUser parseToken(String token) {
		Claims claims=Jwts.parser()
						.verifyWith(getSigninKey())
						.build()
						.parseSignedClaims(token)
						.getPayload();
		
		String userId=claims.getSubject();
		String username=claims.get("username", String.class);
		String role=claims.get("role", String.class);
		return new LoginUser(userId,username,role);
	}
	//驗證token
	public boolean validateToken(String token) {
		try {
			parseToken(token);
			return true;
		} catch(Exception e) {
			return false;
		}
	}	
}