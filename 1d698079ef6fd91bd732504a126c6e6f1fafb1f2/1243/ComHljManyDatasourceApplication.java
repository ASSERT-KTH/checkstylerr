package com.hlj.many.datasourse;

import com.hlj.many.datasourse.data.DynamicDataSourceRegistrar;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.hlj.many.datasourse.dataresource.dao"})
@EntityScan(basePackages = "com.hlj.many.datasourse.dataresource.dao")
@Import({DynamicDataSourceRegistrar.class}) //导入数据库
public class ComHljManyDatasourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComHljManyDatasourceApplication.class, args);
	}
}