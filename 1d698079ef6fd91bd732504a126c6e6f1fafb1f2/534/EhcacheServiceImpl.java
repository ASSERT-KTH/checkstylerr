package com.hlj.Ehcache.service.impl;

import com.hlj.Ehcache.config.CacheConstants;
import com.hlj.Ehcache.service.EhcacheService;
import com.hlj.common.bean.Person;
import com.hlj.common.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/21  下午1:59.
 */
@Service
public class EhcacheServiceImpl implements EhcacheService{




    @Autowired
    private PersonRepository personRepository;


    //这里的单引号不能少，否则会报错，被识别是一个对象;
    public static final String CACHE_KEY = "'person'";

    /**
     * value属性表示使用哪个缓存策略，缓存策略在ehcache.xml,现在存放在下面的实体类中，在启动的时候自动加载了
     * 也叫缓存存放位置名称，不能为空
     */
    public static final String DEMO_CACHE_NAME = CacheConstants.CACHE_PUBLIC_PERSON;


    /**
     * 保存数据，防止是更新的操作，所以将之前缓存的删除,事实上，我也并没有很成功的实现它，后来明白啦，哈，原来是list集合缓存的时候，添加要删除的哦
     * @param Person
     */

    @CacheEvict(value=DEMO_CACHE_NAME,key=CACHE_KEY)
    public Person save(Person Person){
        return personRepository.save(Person);
    }

    /**
     * 查询数据.
     * @param id
     * @return
     */
    @Cacheable(value=DEMO_CACHE_NAME ,key="'Person_'+#id")
    public Person findById(Long id){
        return personRepository.findOne(id);
    }

    /**
     * 修改数据.
     * 在支持Spring Cache的环境下，与@Cacheable不同的是使用@CachePut标注的方法在执行前不会去检查缓存中是否存在之前执行过的结果，而是每次都会执行该方法，并将执行结果以键值对的形式存入指定的缓存中。
     @CachePut也可以标注在类上和方法上。使用@CachePut时我们可以指定的属性跟@Cacheable是一样的。
     */
    @CachePut(value = DEMO_CACHE_NAME,key = "'Person_'+#person.getId()")
    public Person update(Person person)  {
        Person Person = personRepository.findOne(person.getId());
        Person.setName(person.getName());
        Person.setPwd(person.getPwd());
        return Person;
    }


    /**
     * 删除数据.
     * @param id
     */
    @CacheEvict(value = DEMO_CACHE_NAME,key = "'Person_'+#id")//这是清除缓存.
    public void delete(Long id){
        personRepository.delete(id);
    }

    @Cacheable(value=DEMO_CACHE_NAME ,key=CACHE_KEY)
    public List<Person> listPerson() {
        return personRepository.findAll();
    }
}
