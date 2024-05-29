package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	UserRepository repo;
     @RequestMapping("/")
	  public String home(Model model) {
		  
		  return "home";
	  }
     @RequestMapping("/about")
	  public String about(Model model) {
		  
		  return "about";
	  }
     @RequestMapping("/signup")
	  public String signup(Model model) {
		  model.addAttribute("title","Register-Smart Contact Manager");
		  model.addAttribute("user",new User());
		  return "signup";
	  }
     //Handler for registering user
     @RequestMapping(value="/do_register" , method=RequestMethod.POST)
     public String registerUser(@ModelAttribute("user")User user,@RequestParam(value="agreement",defaultValue="false") boolean agreement,Model model,HttpSession session) {
    try {
    	
    	if(!agreement) {
    		System.out.println("Sad (: You have not agreed the terms and conditions !");
    		throw new Exception("Sad (: You have not agreed the terms and conditions !");
    	}
    	 user.setRole("ROLE_USER");
    	 user.setEnabled(true);
    	 user.setImageUrl("default.png");
    	 user.setPassword(passwordEncoder.encode(user.getPassword()));
    	 
    	 
    	 System.out.println("Agreement "+agreement);
    	 System.out.println("User "+user);
    	 User result=this.repo.save(user);
    	 model.addAttribute("user", new User());
     	session.setAttribute("message",new Message("Registration Done Sucessfully :)","alert-success"));
     	return "signup";
    	 
    }
    catch(Exception e) {
    	
    	e.printStackTrace();
    	model.addAttribute("user", user);
    	session.setAttribute("message",new Message("Something went Wrong !!"+e.getMessage(),"alert-danger"));
    	return "signup";
    }
    	 
     }
     
     //Handler for custom login
     @GetMapping("/login")
     public String customLogin(Model model) {
    	 model.addAttribute("title", "Login Page");
    	 return "login";
     }
}
