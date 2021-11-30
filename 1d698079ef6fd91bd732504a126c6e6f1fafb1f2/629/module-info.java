import com.hlj.java9.api.MyServiceInter;

module two {
     requires one;

     //使用接口的名称，上面已经导入了one模块了
     uses MyServiceInter  ;
}