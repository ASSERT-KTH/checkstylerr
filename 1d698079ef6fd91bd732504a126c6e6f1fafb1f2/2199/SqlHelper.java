package com.hlj.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
 
import javax.crypto.spec.PSource;import javax.xml.bind.ParseConversionEvent;
  
public class SqlHelper {
 private Connection ct=null;
 private ResultSet rs=null;
 private PreparedStatement ps=null;
  
  //���������ͨ�� selectѡ��õ��Ķ���������������ѡ�����еģ�
   public   ArrayList executeQuery(String sql,String paras[]){
    //�����ʾ�����  
	   ArrayList  al = new ArrayList();
	   System.out.println("wo 正在操作数据库");
       try{
    	 ct = SqlHelper1.getConnection();
        ps = ct.prepareStatement(sql);  
     /*   for(int i=0;i<paras.length;i++){ 
       ps.setString(i+1,paras[i]);  
        } */  
        rs=ps.executeQuery();
        ResultSetMetaData rsmd=rs.getMetaData();
        
        //ͨ���������rsmd�õ� �ý���ж�����
         
        int columnNum=rsmd.getColumnCount();
        while(rs.next()){ 
         Object objects[] = new Object[columnNum];
         
         for (int i = 0; i < objects.length; i++) {
        	 objects[i]=rs.getObject(i+1);
    }
         al.add(objects);
        }
       }catch(Exception e){
         e.printStackTrace();
     }finally{
         SqlHelper1.close(rs, ps, ct); 
     }
  return al;
   }
}