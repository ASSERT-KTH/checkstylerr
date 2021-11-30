package com.hlj.Ehcache.service;

import com.hlj.common.bean.Person;

import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/21  下午1:59.
 */
public interface EhcacheService {

    Person save(Person Person);

    Person findById(Long id);

    Person update(Person updated);

    void delete(Long id);

    List<Person> listPerson();

}
