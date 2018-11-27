package com.qi.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class CustomerDetailsService implements UserDetailsService {
	/**
	 * 模拟两个账户：
	 * ① 账号是user，密码是password1，角色是user-role
	 * ② 账号是admin，密码是password2，角色是admin-role
	 */
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		if ("user".equals(userName)){
			return new SecurityUser("user", "password1", "user-role");
		}else if ("admin".equals(userName)) {
			return new SecurityUser("admin", "password2", "admin-role");
		} else {
			return null;
		}
	}
}

class SecurityUser implements UserDetails{
	public SecurityUser(String userName, String password, String role){
		super();
		this.userName = userName;
		this.password = password;
		this.role = role;
	}
	public SecurityUser(){

	}
	private Long id;
	private String userName;
	private String password;
	private String role;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(this.role);
		authorities.add(authority);
		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	public String getUserName(){
		return this.userName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}