package com.hlj.springboot.dome.common.moudle.controller;


import com.hlj.springboot.dome.common.data.ResponseBean;
import com.hlj.springboot.dome.common.entity.DemoEntity;
import com.hlj.springboot.dome.common.entity.repository.DemoEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  上午10:22.
 */
@Controller
public class HomeController {

    public static int i = 1;

    @Autowired
    private DemoEntityRepository demoEntityRepository;



    //http://localhost:8080/insert?name=HealerJean
    @GetMapping("insert")
    public @ResponseBody
    ResponseBean insert(DemoEntity demoEntity){
        try {
            return  ResponseBean.buildSuccess(demoEntityRepository.save(demoEntity));
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @GetMapping("findById")
    @ResponseBody
    public ResponseBean  findById(Long id){
        try {
            return ResponseBean.buildSuccess(demoEntityRepository.findOne(id));
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

    @GetMapping("static")
    @ResponseBody
    public ResponseBean  staticTest( ){
        i ++;

        try {
            return ResponseBean.buildSuccess(i);
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }
//http://localhost:8080/updateLockTest?name=healerjean&id=1&version=5
    @GetMapping("updateLockTest")
    @ResponseBody
    public ResponseBean  updateLockTest( String name,Long id ,Long version){

        try {

            for(int i = 1; i<30 ;i++ ){
                new Thread(  ()->{
                    int m =  demoEntityRepository.updateLockTest(name,id,version);
                    System.out.println(m);
                }).start();

            }

            return ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


}
