package com.hlj.moudle.produce.service.impl;

import com.hlj.dao.db.DemoEntityRepository;
import com.hlj.dao.mybatis.demo.DemoEntityMapper;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.produce.service.ProcedureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j
public class ProcedureServiceImpl implements ProcedureService {

    @Resource
    private DemoEntityMapper demoEntityMapper ;

    @Resource
    private DemoEntityRepository demoEntityRepository ;


    /**
     * 1、获取 存储过程out参数值
     * @param id
     * @return
     */
    @Override
    public Integer procedureGetOut(Long id) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", id) ;
        //执行完存储过程会自动更新这个map值
        demoEntityMapper.procedureGetOut(param);
        return Integer.valueOf(param.get("userCount").toString());
    }

    /**
     * 2、获取 存储过程的结果集合-只有一个
     * @param name
     * @return
     */
    @Override
    public List<DemoEntity> procedureGetOneList(String name) {
        Map<String, Object> param = new HashMap<>();
        param.put("userName", name) ;

        //获取结果集
        List<DemoEntity> list  = demoEntityMapper.procedureGetOneList(param);
        System.out.println(Integer.valueOf(param.get("userCount").toString()));

        return list;
    }

    /**
     * 3、获取 存储过程的 获取多个集合 使用了泛型，有可能集合中是不同的
     * @param
     * @return
     */
    @Override
    public List<List<?>> procedureGetManyList(String oneName, String twoName) {
        Map<String, Object> param = new HashMap<>();
        param.put("oneName", oneName) ;
        param.put("twoName", twoName) ;
        List<List<?>> lists =   demoEntityMapper.procedureGetManyList(param) ;
        System.out.println("数组大小"+lists.size());
        System.out.println("某个数量"+Integer.valueOf(param.get("userCount").toString()));
        return lists;
    }


    @Override
    public Integer jpaProcedureGetOut(Long id) {
        return null;
    }

    @Override
    public List<DemoEntity> jpaProcedureGetOneList(String name) {
        return null;
    }

    @Override
    public List<List<?>> jpaProcedureGetManyList(String oneName, String twoName) {
        return null;
    }
}
