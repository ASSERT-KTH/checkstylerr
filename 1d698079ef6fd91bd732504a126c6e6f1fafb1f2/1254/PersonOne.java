package com.hlj.many.datasourse.dataresource.dao.db.entry;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 测试实体类，这个随便;
 */
@Entity
@Table(name = "person_one")
public class PersonOne implements Serializable{
	private static final Long serialVersionUID = 1L;
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String pwd;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public PersonOne() {
	}

	public PersonOne(Long id, String name, String pwd) {
		this.id = id;
		this.name = name;
		this.pwd = pwd;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", pwd=" + pwd + "]";
	}
}
