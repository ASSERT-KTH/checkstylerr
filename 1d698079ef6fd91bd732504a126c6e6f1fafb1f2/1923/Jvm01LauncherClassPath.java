package com.hlj.moudle.Jvm03类加载器;


import org.junit.Test;
import sun.misc.Launcher;

/**
 * @Description 类加载器
 * @Author HealerJean
 * @Date 2019/2/8  下午1:58.
 */
public class Jvm01LauncherClassPath {

    //它是一个java虚拟机的入口应用。放在这里只是为了、观察源码
    public static Launcher  launcher = null ;


    @Test
    public void bootStramClassPath() {
        System.out.println(System.getProperty("sun.boot.class.path"));

        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/resources.jar:
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/rt.jar:
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/sunrsasign.jar:
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/jsse.jar:
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/jce.jar:
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/charsets.jar:
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/jfr.jar:
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/classes
    }

    @Test
    public void ExtClassLoaderClassPath() {
        System.out.println(System.getProperty("java.ext.dirs"));

        //  /Users/healerjean/Library/Java/Extensions:
        //  /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/ext:
        //  /Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:
        //  /usr/lib/java

    }

    @Test
    public void AppClassLoaderClassPath() {
        System.out.println(System.getProperty("java.class.path"));
    }


}