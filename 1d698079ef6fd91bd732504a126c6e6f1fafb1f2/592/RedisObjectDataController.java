package com.hlj.redis.controller;

import com.hlj.common.Format.ResponseBean;
import com.hlj.common.bean.Person;
import com.hlj.redis.redisTool.RedisObjectData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


@Controller
public class RedisObjectDataController {

	@Autowired
	private RedisObjectData redisObjectData;

	/**
	 * 添加缓存对象
	 * @param id
	 * @return
	 */
    @RequestMapping("/setRedisObjectData")
    public @ResponseBody ResponseBean setRedisObjectData(Long id){
		try {
			Person person = new Person();
			person.setName("HealerJean");
			person.setPwd("123456");
			person.setId(id);
			redisObjectData.setData("person", person);
			return  ResponseBean.buildSuccess();
		}catch (Exception e){
			return ResponseBean.buildFailure(e.getMessage());
		}
    }



	/**
	 * 添加List缓存对象
	 * @param id
	 * @return
	 */
	@RequestMapping("/setListRedisObjectData")
	public @ResponseBody ResponseBean setListRedisObjectData(Long id){
		try {
			List<Person> persons = new ArrayList<>();
			Person person1= new Person(id,"HealerJean"+id,"password"+id);
			Person person2 = new Person(id+1,"HuangLiang"+id,"HuangLiang"+id);
			persons.add(person1);
			persons.add(person2);
			redisObjectData.setData("persons", persons);
			return  ResponseBean.buildSuccess();
		}catch (Exception e){
			return ResponseBean.buildFailure(e.getMessage());
		}
	}


	/**
	 * 根据key获取缓存对象
	 * @return
	 */
	@RequestMapping("/getRedisObjectData")
	public @ResponseBody ResponseBean getRedisObjectData(String key){
    	try {
			Person person = (Person) redisObjectData.getData(key);
			return ResponseBean.buildSuccess(person);
		}catch (Exception e){
    		return  ResponseBean.buildFailure(e.getMessage());
		}
	}

	/**
	 * 根据key获取缓存List对象
	 * @return
	 */
	@RequestMapping("/getListRedisObjectData")
	public @ResponseBody ResponseBean getListRedisObjectData(String key){
		try {
			List<Person> persons = (List<Person>) redisObjectData.getData(key);
			return ResponseBean.buildSuccess(persons);
		}catch (Exception e){
			return  ResponseBean.buildFailure(e.getMessage());
		}
	}


	/**
	 * 根据key删除缓存对象
	 * @return
	 */
	@RequestMapping("/delRedisObjectData")
	public @ResponseBody ResponseBean delRedisObjectData(String key){
		try {
			redisObjectData.delete(key);
			return ResponseBean.buildSuccess();
		}catch (Exception e){
			return  ResponseBean.buildFailure(e.getMessage());
		}
	}


/***********************************************************************************************************/



	/**
	 * 添加Long类型缓存对象，这是个错误的演示，仅供测试
	 * @param id
	 * @return
	 */
	@RequestMapping("/set")
	public @ResponseBody ResponseBean set(Long id){
		try {

			redisObjectData.setData("id", id);
			return  ResponseBean.buildSuccess();
		}catch (Exception e){
			return ResponseBean.buildFailure(e.getMessage());
		}
	}

	/**
	 * 根据key获取Long，这是个错误的演示，仅供测试
	 * @return
	 */
	@RequestMapping("/get")
	public @ResponseBody ResponseBean get(String key){
		try {
			Long id = (Long) redisObjectData.getData("id");
			return ResponseBean.buildSuccess(id);
		}catch (Exception e){
			return  ResponseBean.buildFailure(e.getMessage());
		}
	}



}
