package com.ctrip.framework.apollo.demo.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class AnnotationApplication {
  public static void main(String[] args) {
    new AnnotationConfigApplicationContext(AnnotationApplication.class.getPackage().getName());
    onKeyExit();
  }
  private static void onKeyExit() {
    System.out.println("Press any key to exit...");
    try {
      System.in.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
