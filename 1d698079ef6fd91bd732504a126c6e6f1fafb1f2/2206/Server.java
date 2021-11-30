import java.net.*;
import java.io.*;
public class Server{
	public static  void main(String args[]) throws Exception{
		//这个需要一个网络端口，设置一个网络服务器端口
		ServerSocket server = new ServerSocket(8088);
			//取得服务器端口
		Socket sock = server.accept();
		
		FileInputStream inputStream = new FileInputStream("F:/宇晋/常用文件及文档/自学/JSP/jsp视频/自己的服务器/a.html");
	//因为是从端口中写入东西，所以是端口中得到输出流
		OutputStream outStream = sock.getOutputStream();
		
		int len = 0;
				byte[] text = new byte[1024];
		len = inputStream.read(text);
		while(len!=-1){
					outStream.write(text,0,len);
		}

			inputStream.close();
			outStream.close();
			sock.close();
			server.close();
	}
}