package com.hlj.entity.db.demo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 测试实体类;
 */
@Entity
@Table(name = "demo_entity")
@Data
@Accessors(chain = true)
@ApiModel(value = "demo实体类")
public class DemoEntity{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "demo 主键")
	private Long id;

	@ApiModelProperty(value = "姓名")
	private String name;

	@ApiModelProperty(value = "年龄")
	private Long age ;


	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP",insertable = true,updatable = false)
	private Date cdate;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date udate;

}

/**

 drop table demo_entity;

 CREATE TABLE `demo_entity` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `name` varchar(255)   DEFAULT NULL,
 `age` bigint(20) DEFAULT '0',
 `cdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
 `udate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY (`id`)
 );
 */
