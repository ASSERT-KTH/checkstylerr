package com.hlj.springboot.dome.common.entity.repository;

import com.hlj.springboot.dome.common.entity.DemoEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DemoEntityRepository extends CrudRepository<DemoEntity,Long> {

    List<DemoEntity> findAll();



    /**
     * 乐观锁
     * @param name
     * @param id
     * @param version
     * @return
     */
    @Modifying
    @Query(
            value = "UPDATE demo_entity p set  p.name = ?1 ,p.version = p.version + 1  WHERE  p .id = ?2 and p.version = ?3",nativeQuery = true
    )
    @Transactional
    int updateLockTest(String name,Long id ,Long version);


}
