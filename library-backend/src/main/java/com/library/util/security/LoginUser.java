package com.library.util.security;

public class LoginUser {
	
	private String userId;
	private String username;
	private String role;
	
	public LoginUser(String userId, String username, String role) {
		super();
		this.userId = userId;
		this.username = username;
		this.role = role;
	}
	//建立之後不修改而只抓資料
	public String getUserId() {
		return userId;
	}
	public String getUsername() {
		return username;
	}
	public String getRole() {
		return role;
	}
	//簡單判斷role
	public boolean isAdmin() {
		return role.equals("ADMIN");
	}
	public boolean isReader() {
		return role.equals("READER");
	}

}
