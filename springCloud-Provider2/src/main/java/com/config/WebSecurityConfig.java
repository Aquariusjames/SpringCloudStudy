package com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception{
	 	//所有的请求，都需要经过http basic认证
		httpSecurity.authorizeRequests().anyRequest().authenticated().and().httpBasic();
	 }
	@Bean
	public PasswordEncoder passwordEncoder(){
		//明文编码器
		 return NoOpPasswordEncoder.getInstance();
	 }
	 @Autowired
	 private CustomerDetailsService customerDetailsService;
	@Override
	protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception{
		authenticationManagerBuilder.userDetailsService(customerDetailsService).passwordEncoder(this.passwordEncoder());
	}

	@Component
	class CustomerDetailsService implements UserDetailsService {

		@Override
		public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
			if ("user".equals(userName)){
				return new SecurityUser();
			}
			return null;
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
}
