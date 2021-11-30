package com.hlj.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Student entity. @author MyEclipse Persistence Tools
 */

public class Student implements java.io.Serializable {

	// Fields

	private Integer sid;
	private String sname;
	private String ssex;
	private String adept;
	private Integer sage;
	private String saddress;
	
//通过这个集合表明一个学生可以选择多门课程，反过来一门课程可以被多次选择
	private Set stucourses = new HashSet(0);

	// Constructors

	/** default constructor */
	public Student() {
	}

	/** minimal constructor */
	public Student(String sname, String ssex, String adept, Integer sage,
			String saddress) {
		this.sname = sname;
		this.ssex = ssex;
		this.adept = adept;
		this.sage = sage;
		this.saddress = saddress;
	}

	/** full constructor */
	public Student(String sname, String ssex, String adept, Integer sage,
			String saddress, Set stucourses) {
		this.sname = sname;
		this.ssex = ssex;
		this.adept = adept;
		this.sage = sage;
		this.saddress = saddress;
		this.stucourses = stucourses;
	}

	// Property accessors

	public Integer getSid() {
		return this.sid;
	}

	public void setSid(Integer sid) {
		this.sid = sid;
	}

	public String getSname() {
		return this.sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public String getSsex() {
		return this.ssex;
	}

	public void setSsex(String ssex) {
		this.ssex = ssex;
	}

	public String getAdept() {
		return this.adept;
	}

	public void setAdept(String adept) {
		this.adept = adept;
	}

	public Integer getSage() {
		return this.sage;
	}

	public void setSage(Integer sage) {
		this.sage = sage;
	}

	public String getSaddress() {
		return this.saddress;
	}

	public void setSaddress(String saddress) {
		this.saddress = saddress;
	}

	public Set getStucourses() {
		return this.stucourses;
	}

	public void setStucourses(Set stucourses) {
		this.stucourses = stucourses;
	}

}