package com.hlj;

import org.junit.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/13  下午2:34.
 * 类描述：
 */
public class Solution {




    public List<File> findTxt(File root) {
        // TODO:

        return  null ;
    }


    @Test
    public void test(){
        traverseFolder1("/Users/healerjean/Desktop/faceFile");
    }

    /**
     *
     * @param path 为一个文件夹根目录
     */
    public void traverseFolder1(String path) {
        File file = new File(path);
        int fileNum = 0, folderNum = 0;

        if (file.exists()) {

            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    System.out.println("文件夹:" + file2.getAbsolutePath());
                    list.add(file2);
                    fileNum++;
                } else {
                    System.out.println("文件:" + file2.getAbsolutePath());
                    folderNum++;
                }
            }

            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        list.add(file2);
                        fileNum++;
                    } else {
                        System.out.println(file2.getName());
                        System.out.println("文件:" + file2.getAbsolutePath());
                        folderNum++;
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);

    }

}
