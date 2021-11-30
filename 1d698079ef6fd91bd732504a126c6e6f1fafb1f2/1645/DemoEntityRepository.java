package com.hlj.dao.db;

import com.hlj.entity.db.demo.DemoEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DemoEntityRepository extends CrudRepository<DemoEntity,Long> {

    List<DemoEntity> findAll();

    Optional<DemoEntity> findById(Long id);

    /**
     * 为了验证该sql不会放入缓存
     * @param id
     * @return
     */
    @Query(" FROM DemoEntity d where d.id = :id")
    DemoEntity testCachefindById(@Param("id") Long id) ;
}
