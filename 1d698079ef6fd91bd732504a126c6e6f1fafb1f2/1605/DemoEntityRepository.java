package com.hlj.dao.db;

import com.hlj.entity.db.demo.DemoEntity;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DemoEntityRepository extends CrudRepository<DemoEntity,Long> {

    List<DemoEntity> findAll();

    Optional<DemoEntity> findById(Long id);

//
//    @Procedure
//    void jpaProcedureGetOut(@Param("userId") Long userId);
//
//    @Procedure
//    List<DemoEntity> jpaProcedureGetOneList(@Param("userName")String userName);
//
//    @Procedure
//    List<List<?>> jpaProcedureGetManyList(@Param("oneName")String oneName,@Param("twoName") String twoName) ;
//


}
