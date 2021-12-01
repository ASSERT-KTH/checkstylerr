package com.mashibing.springboot.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;
import com.mashibing.springboot.RespStat;
import com.mashibing.springboot.entity.Account;
import com.mashibing.springboot.entity.Config;
import com.mashibing.springboot.service.AccountService;


/**
 * 用户账户相关
 * @author Administrator
 *
 */

@Controller
@RequestMapping("/account")
public class AccountController {

	
	@Autowired
	AccountService accountSrv;
	
	@Autowired
	Config config;
	
	
	@RequestMapping("login")
	public String login(Model model) {
		
		model.addAttribute("config", config);
		return "account/login";
	}
	

	/**
	 * 用户登录异步校验
	 * @param loginName
	 * @param password
	 * @return success 成功
	 */
	
	
	/**
	 *  /Login 1. 如果首次打开（没有任何参数），展示静态的HTML
	 *         2. 如果有post请求，验证账号密码是否正确
	 * @param loginName
	 * @param password
	 * @param request
	 * @return
	 */
	@RequestMapping("validataAccount")
	@ResponseBody
	public String validataAccount(String loginName,String password,HttpServletRequest request) {
		
		System.out.println("loginName:" + loginName);
		System.out.println("password:" + password);
		
		// 1. 直接返回是否登录成功的结果
		// 2. 返回 Account对象，对象是空的 ，在controller里做业务逻辑
		// 在公司里 统一写法
		
	
		//让service返回对象，如果登录成功 把用户的对象 
		Account account = accountSrv.findByLoginNameAndPassword(loginName, password);
		
		if (account == null) {
			return "登录失败";
		}else {
			// 登录成功
			// 写到Session里
			// 在不同的controller 或者前端页面上 都能使用 
			// 当前登录用户的Account对象
			
			request.getSession().setAttribute("account", account);
			return "success";
		}
	}
	
	
	@RequestMapping("/logOut")
	public String logOut(HttpServletRequest request) {
		
		request.getSession().removeAttribute("account");
		return "index";
	}
	@RequestMapping("/list")
	public String list(@RequestParam(defaultValue = "1") int pageNum,@RequestParam(defaultValue = "5" ) int pageSize,Model model) {
		
		PageInfo<Account>page = accountSrv.findByPage(pageNum,pageSize);
		
		model.addAttribute("page", page);
		return "/account/list";
	}
	
	@RequestMapping("/deleteById")
	@ResponseBody
	public RespStat deleteById(int id) {
		// 标记一下 是否删除成功？  status
		RespStat stat = accountSrv.deleteById(id);
		
		return stat;
	}
	
	
	// FastDFS
	
	
	
	@RequestMapping("/profile")
	public String profile () {
		
		try {
			  File path = new File(ResourceUtils.getURL("classpath:").getPath());
		        File upload = new File(path.getAbsolutePath(), "static/upload/");
		        System.out.println(upload.getAbsolutePath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		return "account/profile";
	}
	
	
	/**
	 * 中文字符
	 * @param filename
	 * @param password
	 * @return
	 */
	@RequestMapping("/fileUploadController")
	public String fileUpload (MultipartFile filename,String password) {
		System.out.println("password:" + password);
		System.out.println("file:" + filename.getOriginalFilename());
		try {
			
		File path = new File(ResourceUtils.getURL("classpath:").getPath());
        File upload = new File(path.getAbsolutePath(), "static/upload/");
        
        System.out.println("upload:" + upload);
        
        filename.transferTo(new File(upload+"/"+filename.getOriginalFilename()));
        
        
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "profile";
	}
	
	
}
