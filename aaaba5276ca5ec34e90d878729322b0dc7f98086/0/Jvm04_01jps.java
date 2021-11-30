package com.hlj.moudle.Jvm04_Command;

public class Jvm04_01jps {

    public static void main(String[] args) {

        final String s1 = new String("aa");
        String s2 = "aabb";
        String s3 = s1 + "bb";
        System.out.println(s3 == s2);

    }

}
