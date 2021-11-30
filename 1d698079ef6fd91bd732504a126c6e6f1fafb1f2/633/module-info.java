import com.hlj.java9.api.MyServiceInter;
import com.hlj.java9.api.impl.MyServiceInterImpl;
import com.hlj.java9.api.impl.MyServiceInterImplTwo;

module one {

   //导出可用包
     exports  com.hlj.java9.can;

     //对外提供的接口服务 ,下面指定的接口以及提供服务的impl
     exports  com.hlj.java9.api;
     provides MyServiceInter  with MyServiceInterImpl, MyServiceInterImplTwo;
}