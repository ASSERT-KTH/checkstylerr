package MyTools;
import java.security.*;



public class Md5 {

public static void main(String []args){
  
  
   String test="12fdsafadsf3";
  
   //创建一个MD5Tool类
   Md5 myMd5 =new Md5(); 
   
   String result=myMd5.MD5Encrypt(test);
  
   System.out.println (test+" 加密後的结果是:"+result);
  
}


//该方法将你输入的字符串，通过md5加密，返回一个加密後的字符串
public static String MD5Encrypt(String inStr) {  
  
   MessageDigest md = null;  
   String outStr = null;  
   try {   

    md = MessageDigest.getInstance("MD5");        //可以选中其他的算法如SHA   
    byte[] digest = md.digest(inStr.getBytes());       
    //返回的是byet[]，要转化为String存储比较方便  
    outStr = bytetoString(digest);  
   } 
   catch (NoSuchAlgorithmException nsae) {   
    nsae.printStackTrace();  
   }  
   return outStr; 
} 

public static String bytetoString(byte[] digest) {  

   String str = "";  
   String tempStr = "";  
   for (int i = 1; i < digest.length; i++) {   
    tempStr = (Integer.toHexString(digest[i] & 0xff));   
    if (tempStr.length() == 1) {    
     str = str + "0" + tempStr;   
    } 
    else {    
     str = str + tempStr;   
    }  
   }  
   return str.toLowerCase(); 

}
}
