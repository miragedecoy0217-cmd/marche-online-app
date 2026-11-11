package com.marche.model;

public class AdminUser {

	private int adminId;
	private String loginId;
	private String passwordHash;
	private String name;
	private String role; // superadmin / product / order / review / inventory

	public int getAdminId() { return adminId; }
	public void setAdminId(int adminId) { this.adminId = adminId; }
	public String getLoginId() { return loginId; }
	public void setLoginId(String loginId) { this.loginId = loginId; }
	public String getPasswordHash() { return passwordHash; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
}