package om.mashibing.springboot02.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import om.mashibing.springboot02.entity.City;
import om.mashibing.springboot02.service.CityService;

@Controller
@RequestMapping("/city")
public class MainController {

	@Autowired
	CityService citySrv;
	
	@RequestMapping("/list")
	public String list(Model map) {
		
		List<City> list =  citySrv.findAll();
		
		map.addAttribute("list", list);
		return "list";
	}
	

}
