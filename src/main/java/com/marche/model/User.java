package com.marche.model;

public class User {
	
	private int user_id;
	private String password_hash;
	private String email;
	private String name;
	private String role;
	private java.time.LocalDateTime created_at;
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getPassword_hash() {
		return password_hash;
	}
	public void setPassword_hash(String password_hash) {
		this.password_hash = password_hash;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public java.time.LocalDateTime getCreated_at() {
		return created_at;
	}
	public void setCreated_at(java.time.LocalDateTime created_at) {
		this.created_at = created_at;
	}

}
