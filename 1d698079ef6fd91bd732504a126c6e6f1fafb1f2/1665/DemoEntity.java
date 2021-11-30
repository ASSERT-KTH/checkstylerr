package com.hlj.entity.db.demo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 测试实体类;
 */
@Entity
@Table(name = "demo_entity")
@Data
@Accessors(chain = true)
@ApiModel(value = "demo实体类")
@Cacheable //注解标记该entity开启 二级缓存，
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region ="cacheSerializer.hibernate.twocache")//注解指定缓存策略，以及存放到哪个缓存区域。
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
