package cn.itcast.action;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;

import cn.itcast.bean.Person;
import freemarker.template.utility.Execute;

/*
 * 下面这个是测试的时候的
 * 
 * public class HelloWorldAction {
	private String msg;
	
	public String getMessage() {
		return msg;
	}

	public String execute(){
		msg = "这是我们的第一个 Struts 2 应用";
		
		return "success";
	}
}*/


/*public class HelloWorldAction {
	private String msg;
	private String username;
	
	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getMessage() {
		return msg;
	}


	public String execute() throws Exception{
		this.username = URLEncoder.encode("传智播客", "UTF-8");
		this.msg = "我的第一个struts2应用";
		return "success";
	}
	
	public String add(){
		return "message";
	} 
}*/


/**
 * 依赖注入开始
 * 
 * @author 荒凉
 *
 */
/*public class HelloWorldAction {
	
	String pathString;

	public String getPathString() {
		return pathString;
	}

	public void setPathString(String pathString) {
		this.pathString = pathString;
	}
	public String execute() throws Exception{
	
		
		return "success";
	}
	
}*/


/**
 * 动态方法调用
 * 
 */
/*public class HelloWorldAction {
	private String msg;
	
	public String getMsg() {
		return msg;
	}

	public String addUI(){
		msg = "addUI";
		return "success";
	}

	public String execute() throws Exception{
		msg = "execute";
		return "success";
	}
}*/



/**
 * 接收请求参数
 * @author 荒凉
 *
 */

/*public class HelloWorldAction {//?id=23&name=xxx
	private Integer id;
	private String name;
	private Person person;
	 
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String addUI(){
		return "success";
	} 

	public String execute() throws Exception{
		return "success";
	}
}*/


/**
 * 自定义类型转化器
 * @author 荒凉
 *
 */
/*
public class HelloWorldAction {//?birthday=2008-10-10
	private Date birthday;
	
	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		System.out.println(birthday);
		this.birthday = birthday;
	}

	public String addUI(){
		return "success";
	}

	public String execute() throws Exception{
		return "success";
	}
}
*/

/**
 * setvlet访问或者添加几个属性
 */

/*public class HelloWorldAction {
	//getApplication
	//一般情况下使用下面这种方法
	public String execute(){
		ActionContext ctx = ActionContext.getContext();
		ctx.getApplication().put("app", "应用范围");//往ServletContext里放入app
		ctx.getSession().put("ses", "session范围");//往session里放入ses
		ctx.put("req", "request范围");//往request里放入req
		ctx.put("names", Arrays.asList("老张", "老黎", "老方"));
		return "success";
	}
	
	public String rsa() throws Exception{
		//要得到摸一个路径
		//servletContext
		HttpServletRequest request = ServletActionContext.getRequest();
		ServletContext servletContext = ServletActionContext.getServletContext();
		request.setAttribute("req", "请求范围属性");
		request.getSession().setAttribute("ses", "会话范围属性");
		servletContext.setAttribute("app", "应用范围属性");
		//HttpServletResponse response = ServletActionContext.getResponse();
		return "success";
	}

}*/





/**
 * 上传下载
 * @author 荒凉
 *
 */
/*
public class HelloWorldAction {
	private File image;
	private String imageFileName;
	private String imageContentType;
	public String getImageContentType() {
		return imageContentType;
	}

	public void setImageContentType(String imageContentType) {
		this.imageContentType = imageContentType;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public String addUI(){
		return "success";
	}

	public String execute() throws Exception{
		System.out.println("文件的类型"+imageContentType);
		String realpath = ServletActionContext.getServletContext().getRealPath("/images");
		System.out.println(realpath);
		if(image!=null){
			File savefile = new File(new File(realpath), imageFileName);
			System.out.println(savefile.getParentFile()+"*******是一个路径**************");
			if(!savefile.getParentFile().exists()) savefile.getParentFile().mkdirs();
			FileUtils.copyFile(image, savefile);
			ActionContext.getContext().put("message", "上传成功");
		}
		return "success";
	}
} */




/**
 * 多文件上传
 * @author 荒凉
 *
 */
/*
public class HelloWorldAction {
	private File[] image;
	private String[] imageFileName;

	public File[] getImage() {
		return image;
	}

	public void setImage(File[] image) {
		this.image = image;
	}

	public String[] getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String[] imageFileName) {
		this.imageFileName = imageFileName;
	}

	public String addUI(){
		return "success";
	}

	public String execute() throws Exception{
		
		String realpath = ServletActionContext.getServletContext().getRealPath("/images");
		System.out.println(realpath);
		if(image!=null){
			File savedir = new File(realpath);
			if(!savedir.exists()) savedir.mkdirs();
			for(int i = 0 ; i<image.length ; i++){				
				File savefile = new File(savedir, imageFileName[i]);
				FileUtils.copyFile(image[i], savefile);
			}
			ActionContext.getContext().put("message", "上传成功");
		}
		return "success";
	}
}

*/




/**
 * 自定义拦截器
 * @author 荒凉
 *
 */
public class HelloWorldAction {	
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String addUI(){
		this.message = "addUI";
		return "success";
	}

	public String execute() throws Exception{
		this.message = "execute";
		return "success";
	}
}
