package com.hlj.moudle.servletcontext;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/20  下午1:07.
 * 类描述：
 */
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * ServletContext监听器和ServletContext属性监听器
 *
 */
@WebListener
public class ContextListener implements ServletContextAttributeListener, ServletContextListener {

    /**
     * 当Servlet 容器终止Web 应用时调用该方法。在调用该方法之前，容器会先销毁所有的Servlet 和Filter 过滤器。
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("ServletContext destroyed");
    }

    /**
     * 当Servlet 容器启动Web 应用时调用该方法。在调用完该方法之后，容器再对Filter 初始化，
     * 并且对那些在Web 应用启动时就需要被初始化的Servlet 进行初始化。
     *
     * 作用：做一些初始化的内容添加工作、设置一些基本的内容、比如一些参数或者是一些固定的对象等等。
     *
     * 1、ServletContext 对象是一个为整个 web 应用提供共享的内存，任何请求都可以访问里面的内容
     * 2、如何实现在服务启动的时候就动态的加入到里面的内容：我们需要做的有：  
     *  1 ） 实现 servletContextListerner 接口 并将要共享的通过 setAttribute （ name,data ）方法提交到内存中去   ；
     *  2 ）应用项目通过 getAttribute(name) 将数据取到 。
     *
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext sct=sce.getServletContext();
        Map<Integer,String> depts=new HashMap<Integer,String>();
        sct.setAttribute("dept", depts);

        System.out.println("ServletContext initialized");
    }



    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        System.out.println("ServletContext attribute added");
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
        System.out.println("ServletContext attribute removed");
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        System.out.println("ServletContext attribute replaced");
    }

}