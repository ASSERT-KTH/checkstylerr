package projects.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@SpringBootApplication(exclude=GroovyTemplateAutoConfiguration.class)
//@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
//@SpringBootApplication
public class ProjectStarter {
	
	@RequestMapping("/")
    String home() {
        return "index";
    }

    public static void main(String[] args) {
        SpringApplication.run(ProjectStarter.class, args);
    }
}
