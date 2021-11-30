package com.hlj.springboot.dome.common.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 测试实体类，这个随便;
 */
@Entity
@Table(name = "demo_entity")
@Data
@Accessors(chain = true)
public class DemoEntity implements Serializable{
	private static final Long serialVersionUID = 1L;
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Long balance;



}
