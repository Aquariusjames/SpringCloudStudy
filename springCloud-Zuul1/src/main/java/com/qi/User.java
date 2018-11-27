package com.qi;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public class User {
	private Long id;
	private String username;
	private String name;
	private Integer age;
	private BigDecimal balance;

	public User() {
	}

	public User(String username, Integer age) {
		this.username = username;
		this.age = age;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equal(id, user.id) &&
				Objects.equal(username, user.username) &&
				Objects.equal(name, user.name) &&
				Objects.equal(age, user.age) &&
				Objects.equal(balance, user.balance);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, username, name, age, balance);
	}
}
