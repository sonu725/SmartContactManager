package com.spring.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.spring.dao.UserRepository;
import com.spring.entities.Contact;
import com.spring.entities.User;
import com.spring.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model mo) {

		mo.addAttribute("title", "Home - Smart Contact Manager");
		System.out.println("Welcome to home controller");
		return "home";
	}

	@RequestMapping("/signup")
	public String signup(Model mo) {

		mo.addAttribute("title", "Register - Smart Contact Manager");
		mo.addAttribute("user", new User());
		System.out.println("Welcome to Register controller");
		return "signup";
	}
	// sign in
	
	@GetMapping("/signin")
	public String login(Model model, HttpSession session) {
		model.addAttribute("userLogin", true);
		model.addAttribute("title", "Login - Smart Contact Manager");
		if(session.getAttribute("validation") != null) {
			String validationStatus = (String)session.getAttribute("validation");
			System.out.println(validationStatus);	
		}
		return "login";
	}

//	Handler for registration

	@PostMapping("/do_register")
	public String registeruser(@Valid @ModelAttribute("user") User user, BindingResult results,@RequestParam("image") MultipartFile file,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model mo,
			HttpSession session) {

		try {
			if (!agreement) {
				mo.addAttribute("flag", true);
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
				
			}

			if (results.hasErrors()) {
				System.out.println("hello");
				System.out.println("Error" + results.toString());
				mo.addAttribute("user", user);

				return "signup";
			}
			

			if (file.isEmpty()) {
				// if file is empty then try our message
				System.out.println("File is Empty........");
				user.setImageUrl("contact.jpg");
				// throw new Exception("File should not be empty!!");
			} else {
				// read file and write code for uploading the file

				user.setImageUrl(file.getOriginalFilename());
				File fsFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(fsFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded.......");
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
//			user.setImageUrl("sonu.png");
			System.out.println(user.getPassword());
			user.setPassword(this.passwordEncoder.encode(user.getPassword()));
			System.out.println("Agreement is " + agreement);
			System.out.println("User is " + user);
			User result = this.userRepository.save(user);
			mo.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			return "signup";
		} catch (Exception e) {

			e.printStackTrace();
			mo.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong !!" + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}
	
	@RequestMapping("/test")
	public String test(Model mo) {
		
		mo.addAttribute("title","this is test page");
		return "Normal/dummy";
	}
	

}
