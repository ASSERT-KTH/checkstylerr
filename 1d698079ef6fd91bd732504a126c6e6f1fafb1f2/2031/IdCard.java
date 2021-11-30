package com.hlj.domain;

import java.util.Date;

public class IdCard implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Date validateDte;
	private Person person;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getValidateDte() {
		return validateDte;
	}
	public void setValidateDte(Date validateDte) {
		this.validateDte = validateDte;
	}
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
	}
}
