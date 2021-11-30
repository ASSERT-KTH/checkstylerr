package cn.bjsxt.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class GameUtil {
	
	private GameUtil(){}  //工具类最好将构造器私有了。
	
	public static Image getImage(String path){
		BufferedImage bi=null;
		try {
			URL u = GameUtil.class.getClassLoader().getResource(path);
			bi = javax.imageio.ImageIO.read(u);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bi;
	}
}
