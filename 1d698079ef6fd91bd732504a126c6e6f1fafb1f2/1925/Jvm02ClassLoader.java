package com.hlj.moudle.Jvm03类加载器;

import org.junit.Test;

/**
 * @Description
 * @Author HealerJean
 * @Date 2019/2/8  下午3:45.
 */
public class Jvm02ClassLoader {


    /**
     * 1、 也就是说明Jvm02Test.class文件是由AppClassLoader加载的。
     */
    @Test
    public  void testClassLoader (){

        ClassLoader classLoader = Jvm02Test.class.getClassLoader();

        System.out.println("ClassLoader is:" + classLoader.toString());
        // ClassLoader is:sun.misc.Launcher$AppClassLoader@18b4aac2
    }


    /**
     * 2、
     提示的是空指针，意思是int.class这类基础类没有类加载器加载？<br/>

     当然不是！
     int.class是由Bootstrap ClassLoader加载的。要想弄明白这些，我们首先得知道一个前提。<br/>
     <font color="red">每个类加载器都有一个父加载器,通过getParent方法</font>

     */
    @Test
    public void intClassLoader(){

      ClassLoader  classLoader = int.class.getClassLoader() ;
      System.out.println("int is:" + classLoader.toString());
      //空指针异常
      //java.lang.NullPointerException

      ClassLoader stringClassLoader = String.class.getClassLoader() ;
      System.out.println(stringClassLoader.toString());
//     java.lang.NullPointerException


    }



    @Test
    public void classLoaderParent(){

        ClassLoader classLoader = Jvm02Test.class.getClassLoader();

        System.out.println("ClassLoader is:" + classLoader.toString());
//        AppClassLoader
//        打印信息：" ClassLoader is:sun.misc.Launcher$AppClassLoader@18b4aac2

        System.out.println("ClassLoader\'s parent is:"+classLoader.getParent().toString());
//       ExtClassLoader@
//       打印信息："   ClassLoader's parent is:sun.misc.Launcher$ExtClassLoader@531d72ca

/*
        ClassLoader is:sun.misc.Launcher$AppClassLoader@18b4aac2
        ClassLoader's parent is:sun.misc.Launcher$ExtClassLoader@531d72ca
*/
    }


    @Test
    public  void testGrandClassLoader (){

        ClassLoader classLoader = Jvm02Test.class.getClassLoader();

        System.out.println("ClassLoader is:" + classLoader.toString());
        // ClassLoader is:sun.misc.Launcher$AppClassLoader@18b4aac2
        System.out.println("ClassLoader\'s parent is:"+classLoader.getParent().toString());
        //ClassLoader's parent is:sun.misc.Launcher$ExtClassLoader@3941a79c
        System.out.println("ClassLoader\'s grand father is:"+classLoader.getParent().getParent().toString());
        //跑出了异常，空指针
        //Exception in thread "main" java.lang.NullPointerException

    /*
         ClassLoader is:sun.misc.Launcher$AppClassLoader@18b4aac2
         ClassLoader's parent is:sun.misc.Launcher$ExtClassLoader@531d72ca

        java.lang.NullPointerException

     */
    }


}
