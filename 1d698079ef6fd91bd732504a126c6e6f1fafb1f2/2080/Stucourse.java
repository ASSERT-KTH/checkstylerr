package com.hlj.domain;

/**
 * Stucourse entity. @author MyEclipse Persistence Tools
 */

public class Stucourse implements java.io.Serializable {

	// Fields

	private Integer stuCourseId;
	private Student student;
	private Course course;
	private Integer grade;
 
	// Constructors

	/** default constructor */
	public Stucourse() {
	}

	/** full constructor */
	public Stucourse(Student student, Course course, Integer grade) {
		this.student = student;
		this.course = course;
		this.grade = grade;
	}

	// Property accessors

	public Integer getStuCourseId() {
		return this.stuCourseId;
	}

	public void setStuCourseId(Integer stuCourseId) {
		this.stuCourseId = stuCourseId;
	}

	public Student getStudent() {
		return this.student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Course getCourse() {
		return this.course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Integer getGrade() {
		return this.grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

}