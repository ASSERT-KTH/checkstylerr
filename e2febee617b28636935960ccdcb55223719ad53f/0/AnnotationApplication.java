package com.ctrip.framework.apollo.demo.spring;

import com.ctrip.framework.apollo.demo.spring.bean.AnnotatedBean;
import com.ctrip.framework.apollo.demo.spring.config.AppConfig;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Scanner;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class AnnotationApplication {
  public static void main(String[] args) {
    new AnnotationConfigApplicationContext(
        AppConfig.class.getPackage().getName(), AnnotatedBean.class.getPackage().getName());
    onKeyExit();
  }

  private static void onKeyExit() {
    System.out.println("Press Enter to exit...");
    new Scanner(System.in).nextLine();
  }
}
