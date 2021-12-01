package com.mashibing.arica.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import com.jfinal.template.ext.spring.JFinalViewResolver;
import com.mashibing.arica.entity.Item;
import com.mashibing.arica.entity.ItemExample;
import com.mashibing.arica.entity.ItemHtml;
import com.mashibing.arica.mapper.ItemDAO;

@Service
public class ItemService  {

	@Autowired
	ItemDAO itemDao;
	@Value(value = "${nginx.html.root}")
	String htmlRoot;
	
	@Value(value = "${jfinal.templates.location}")
	String templatesLocation;
	
	public Item insert(Item item) {

		itemDao.insert(item);
		return item;
	}

	public Item findById(int id) {
		return itemDao.selectByPrimaryKey(id);		
	}

	public List<Item> findAll() {
		ItemExample example = new ItemExample();
		return itemDao.selectByExample(example );
	}

	public void generateHtml(int id) {

		// 初始化模板引擎
		Engine engine = JFinalViewResolver.engine;
		
		// 从数据源，获取数据
		Item item = itemDao.selectByPrimaryKey(id);
		//c:/dev/uploads/
		
		// 前端模板用的键值对
		Kv kv = Kv.by("item", item);
		
		// 文件写入路径
		String fileName = "item"+id+".html";
		String filePath = htmlRoot;
		// 路径 直接能被用户访问
		File file = new File(filePath+fileName);
		
		// 开始渲染 输出文件
		Template template = engine.getTemplate("item.html");
		template.render(kv, file);
	}

	public String getFileTemplateString() throws Exception{
		// TODO Auto-generated method stub
		
		// tomcat 下部署项目，可以这么取
		// 获取 静态模板文件的输入流
		
//		Engine engine = JFinalViewResolver.engine;
//		Template template = engine.getTemplate("");
//		
//		String toString = template.renderToString();
//		
//		
//		
		String file = templatesLocation + "item.html";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		
		// 读缓冲区
		StringBuffer sb = new StringBuffer();
		
		String lineStr = reader.readLine();
		while (lineStr!=null) {
			sb.append(lineStr).append("\r\n");
			lineStr = reader.readLine();
		}
		reader.close();

		return sb.toString();
	}

	public void saveFileTemplateString(String content)throws Exception {
		
		String file = templatesLocation + "item.html";
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(content);
		writer.flush();
		writer.close();
	}

	public List<ItemHtml> generateAll() {

		// 准备数据
		List<ItemHtml> list = itemDao.selectAllByItemHtml();
		
		// 初始化模板引擎
		Engine engine = JFinalViewResolver.engine;
		// 文件写入路径
		
		String filePath = htmlRoot;
		// 获取模板
		Template template = engine.getTemplate("item.html");
		
		for (ItemHtml item : list) {
			
			Kv kv = Kv.by("item", item);
			// 路径 直接能被用户访问
			String fileName = "item"+item.getId()+".html";
			File file = new File(filePath+fileName);
			
			try {
				// 开始渲染 输出文件
				template.render(kv, file);
				item.setHtmlStatus("ok");
				item.setLocation(htmlRoot + fileName);
			}catch (Exception e) {
				// 记日志
				item.setHtmlStatus("err");
				continue;
			}
		}
		
		return list;
	}

	// 创建系统首页 html
	public void generatemain() {
		// 初始化模板引擎
		Engine engine = JFinalViewResolver.engine;
		
		ItemExample example = new ItemExample();
		// 从数据源，获取数据
		List<Item> items = itemDao.selectByExample(example );
		//c:/dev/uploads/  
		
		// 前端模板用的键值对
		Kv kv = Kv.by("items", items);
		
		// 文件写入路径
		String fileName = "main.html";
		String filePath = htmlRoot;
		// 路径 直接能被用户访问
		File file = new File(filePath+fileName);
		
		// 开始渲染 输出文件
		Template template = engine.getTemplate("item_main.html");
		template.render(kv, file);
		
	}

	public HashMap<String, Boolean> health() throws Exception {
		
		HashMap<String, Boolean> map = new HashMap<>();
		
		map.put("192.168.150.113", null);
		map.put("192.168.150.213", null);
		map.put("192.168.150.133", null);
		
		for (String key : map.keySet()) {
			
			InetAddress inetAddress = InetAddress.getByName(key);
			boolean reachable = inetAddress.isReachable(3000);
			
			map.put(key, reachable);
		}
		return map;
	}

}
