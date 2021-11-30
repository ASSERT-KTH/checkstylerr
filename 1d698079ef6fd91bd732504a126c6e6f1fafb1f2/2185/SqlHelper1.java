package com.hlj.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.sql.*;
  
public class SqlHelper1 
{
    //�������
    private static Connection ct = null;
    //������������preparedstatement���statement
    private static PreparedStatement ps = null;
    private static ResultSet rs = null;
      
    //������ݿ�Ĳ���
    private static String url = "jdbc:mysql://localhost:3306/shouru";
    private static String username = "root";
    private static String driver = "com.mysql.jdbc.Driver";
    private static String passwd = "147094";
   
    private static CallableStatement cs = null;
    public static CallableStatement getCs()
    { 
        return cs;
    }

    private static Properties  pp = null;
    private static InputStream fis = null;
    //������ֻ��Ҫһ�Σ��þ�̬�����(���������ʱ���Զ��ͻ���е�����)
    static
    {
        try
        { 
            Class.forName(driver);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {       }
       
    }
    //�õ�����
    public static Connection getConnection()
        {  
            try
            {ct = DriverManager.getConnection(url,username,passwd);}
            catch(Exception e) {e.printStackTrace();}
            return ct;
        }
   
   
//*************callPro1�洢��̺���1*************   
    public static CallableStatement callPro1(String sql,String[] parameters)
    {
        try{
            ct = getConnection();
            cs = ct.prepareCall(sql);
            if(parameters!=null){
                for(int i=0;i<parameters.length;i++){
                 cs.setObject(i+1,parameters[i]);
                }
            }   
            cs.execute();
        }
        catch(Exception e) { e.printStackTrace(); throw new RuntimeException(e.getMessage());}
        finally
        { close(rs,cs,ct);}
        return cs;
    }
   
//*******************callpro2�洢���2************************
public static CallableStatement callPro2(String sql,String[] inparameters,
Integer[] outparameters)
{
    try
    {
        ct = getConnection();
        cs = ct.prepareCall(sql);
        if(inparameters!=null)
        { 
            for(int i=0;i<inparameters.length;i++)
            {
                cs.setObject(i+1,inparameters[i]);
            }
        }
    //cs.registerOutparameter(2,oracle.jdbc.OracleTypes.CURSOR);
        if(outparameters!=null)
        {
            for(int i=0;i<outparameters.length;i++)
            {
                cs.registerOutParameter(inparameters.length+1+i,outparameters[i]);
            }
        }
        cs.execute();
    }
    catch(Exception e) {
        e.printStackTrace(); throw new RuntimeException(e.getMessage());
    }
    finally
    {
       
    }
    return cs;
}
    public static ResultSet executeQuery(String sql,String[] parameters)
    {
        try
        { 
            ct=getConnection();
            ps=ct.prepareStatement(sql);
            if(parameters!=null)
            {
                for(int i=0;i<parameters.length;i++)
                {
                    ps.setString(i+1,parameters[i]);
                }
            }
            rs = ps.executeQuery();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        finally
        {
           
        }
        return rs;
    }
   
   
    public static Connection getCt()
    {
        return ct;
    }

    public static PreparedStatement getPs()
    {
        return ps;
    }

    public static ResultSet getRs()
    {
        return rs;
    }

   
   
    public static void executeUpdate2(String[] sql,String[][] parameters)
    {
        try
        {
            ct = getConnection();
            ct.setAutoCommit(false);
           
            for(int i=0;i<sql.length;i++)
            {
               
                if(null!=parameters[i])
                {
                    ps = ct.prepareStatement(sql[i]);
                    for(int j=0;j<parameters[i].length;j++)
                    {
                        ps.setString(j+1,parameters[i][j]);
                    } 
                    ps.executeUpdate();
                }
               
            }
           
           
            ct.commit();
           
             
        }catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                ct.rollback();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            throw  new RuntimeException(e.getMessage());
        }finally
        {
            close(rs,ps,ct);
        }
       
    }
   
    //��дһ��update��delete��insert
    //sql��ʽ��update ���� set �ֶ��� =��where �ֶ�=��
    //parameter��Ӧ���ǣ���abc��,23��
    public static void executeUpdate(String sql,String[] parameters)
    {
        try
        {
            ct=getConnection();
            ps = ct.prepareStatement(sql);
            if(parameters!=null)
            {
                for(int i=0;i<parameters.length;i++)
                {
                    ps.setString(i+1,parameters[i]);
                }
                           
            }
            ps.executeUpdate();
        }
        catch(Exception e)
        {
            e.printStackTrace();//�����׶�
            //�׳��쳣
            //���Դ��?Ҳ���Բ�����
            throw new RuntimeException(e.getMessage());
        }
        finally
        {
            close(rs,ps,ct);
        }
    }
   
    public static void close(ResultSet rs,Statement ps,Connection ct)
    {
        //�ر���Դ(�ȿ����)
        if(rs!=null)
        {
            try
            {
                rs.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
            rs=null;
        }
        if(ps!=null)
        {
            try
            {
                ps.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
            ps=null;
        }
        if(null!=ct)
        {
            try
            {
                ct.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
            ct=null;
        }
    }
}
