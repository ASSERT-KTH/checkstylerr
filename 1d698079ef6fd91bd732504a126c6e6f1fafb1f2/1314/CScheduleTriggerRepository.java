package com.hlj.quartz.quartz.Repository;

import com.hlj.quartz.quartz.Bean.CScheduleTrigger;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/23  下午4:17.
 */
public interface CScheduleTriggerRepository extends CrudRepository<CScheduleTrigger,Long>{

    List<CScheduleTrigger> findAll();
}
