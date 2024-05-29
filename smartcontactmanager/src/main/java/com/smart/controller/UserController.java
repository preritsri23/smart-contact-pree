package com.smart.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository repo;
	@Autowired
	private ContactRepository contactRepo;
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String username=principal.getName();
    	System.out.println("username"+username);
    	User user=repo.getUserByUserName(username);
    	System.out.println("User "+user);
    	model.addAttribute("user", user);
	}
	    @RequestMapping("/index")
         public String dashboard(Model model,Principal principal) {
	    
	    	return "normal/user_dashboard";
         }
	    //Open Add form handler
	    @GetMapping("/add-contact")
	    public String openAddContactForm(Model model) {
	    	model.addAttribute("title", "ADD CONTACT");
	    	model.addAttribute("contact", new Contact());
	    	return "normal/add_contact_form";
	    }
	    
	    //Processing contact
	    @PostMapping("/process-contact")
	    public String processContact(@ModelAttribute Contact contact,
	    		@RequestParam("profileImage") MultipartFile file, 
	    		Principal principal,HttpSession session) {
	           try {
	        	 	String name=principal.getName();
	    	    	User user=this.repo.getUserByUserName(name);
	    	    	
	    	    	//Processing and uploading file
	    	    	if(file.isEmpty()) {
	    	    		//Try our msgg!!!!!!!!!!
	    	    		contact.setImageUrl("contact.png");
	    	    	}
	    	    	else {
	    	    		//upload the file to folder and update the namd to contact
	    	    		contact.setImageUrl(file.getOriginalFilename());
	    	    		
	    	    		File saveFile=new ClassPathResource("static/img").getFile();
	    	    		Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	    	    		Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING );
	    	    		System.out.println("Image is uploaded");
	    	    		
	    	    	}
	    	    	          contact.setUser(user);
	    	    	          
	    	    	          
	    	    	          user.getContacts().add(contact);
	    	    	          this.repo.save(user);
	    	    	          
	    	    	
	    	    	System.out.println("Added to database");
	    	    	session.setAttribute("message",new Message("Contact Added Sucessfully :)","alert-success"));
	    	    	
	           }
	           catch(Exception e) {
	                  System.out.println("Error "+e.getMessage());
	                  e.printStackTrace();
	                  session.setAttribute("message",new Message("Ayee ! Error in saving contact (:","alert-danger"));
	           }
	           return "normal/add_contact_form";
	    }
	    
	    // Show contacts handler
	    //Per page=5[n]
	    //current page=0 [current]
	    @GetMapping("/show-contacts/{page}")
	    public String showContacts(@PathVariable("page")Integer page,Model model,Principal principal) {
	    	model.addAttribute("title", "Show Contacts List ");
	    	//Contact ki list ko bhejni hai
	    	String userName=principal.getName();
	    	User user=this.repo.getUserByUserName(userName);
	    	
	                PageRequest pageable= PageRequest.of(page, 5);
	        Page<Contact> contacts=this.contactRepo.findContactsByUser(user.getId(),pageable);
	    	model.addAttribute("contacts", contacts);
	    	model.addAttribute("currentPage",page);
	    	model.addAttribute("totalPages", contacts.getTotalPages());
	    	return "normal/show_contacts";
	    }
	    
	    
	    //Showing specific contact details
	    @RequestMapping("/{cid}/contact")
	    public String showContactDetail(@PathVariable("cid")Integer cid,Model model,Principal principal) {
	    Optional<Contact> contactOptional=	this.contactRepo.findById(cid);
	    Contact contact=contactOptional.get();
	    String userName=principal.getName();
	    User user=this.repo.getUserByUserName(userName);
	    if(user.getId()==contact.getUser().getId())
	    	 model.addAttribute("contact",contact);
	    	return "normal/contact_detail";
	    }
	    
	    
	    //Delete Contact Handler
	    @GetMapping("/delete/{cid}")
	    public String deleteContact(@PathVariable("cid")Integer cid,Principal principal,HttpSession session) {
	    	Optional<Contact> contactOptional=this.contactRepo.findById(cid);
	    	Contact contact=contactOptional.get();
	    	//check.....
	    	  String userName=principal.getName();
	  	    User user=this.repo.getUserByUserName(userName);
	  	    if(user.getId()==contact.getUser().getId()) {
	  	    	contact.setUser(null);
	    	this.contactRepo.delete(contact);
	    	 session.setAttribute("message", new Message("Contact Deleted Succesfully","success"));
	    	}
	  	   
	  	    
	  	    return "redirect:/user/show-contacts/0";
	    	
	    	
	    	
	    }
	    
	    //Open Update form handler
	    @RequestMapping("/update-contact/{cid}")
	    public String updateForm(@PathVariable("cid")Integer cid,Model model) {
	    	Contact contact=this.contactRepo.findById(cid).get();
	    	model.addAttribute("contact",contact);
	    	
	    	  return "normal/update_form";
	    }
	    
	    
	    //Update Operation performing
	    @RequestMapping(value="/process-update",method=RequestMethod.POST)
	    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,Model model,HttpSession session,Principal principal) {
	    	try {
	    		//Old contact detail
	    		Contact oldcontact=this.contactRepo.findById(contact.getCid()).get();
	    		
	    		if(!file.isEmpty()) {
	    			//file work
	    			//rewrite
	    			//Delete old photo
	    			File deleteFile=new ClassPathResource("static/img").getFile();
	    			File file1=new File(deleteFile,oldcontact.getImageUrl());
	    			file1.delete();
	    			//update new photo
	    			File saveFile=new ClassPathResource("static/img").getFile();
    	    		Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
    	    		Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING );
    	    		contact.setImageUrl(file.getOriginalFilename());
    	    		System.out.println("Image is uploaded");
	    			
	    		}
	    		else {
	    			contact.setImageUrl(oldcontact.getImageUrl());
	    		}
	    		User user=this.repo.getUserByUserName(principal.getName());
	    		contact.setUser(user);
	    		this.contactRepo.save(contact);
	    		session.setAttribute("message", new Message("Your Contact got updated...","success"));
	    		
	    		
	    	}
	    	catch(Exception e) {
	    		
	    	}
	    	return "redirect:/user/"+contact.getCid()+"/contact";
	    }
	    
	    
	    //Your Profile Handler
	    @GetMapping("/profile")
	    public String yourProfile() {
	    	return "normal/profile";
	    	
	    }
         
}
