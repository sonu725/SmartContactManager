package com.spring.controller;

import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.spring.dao.ContactRepository;
import com.spring.dao.UserRepository;
import com.spring.entities.Contact;
import com.spring.entities.User;
import com.spring.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class userControllers {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	// method to adding common data
 
	@ModelAttribute
	public void addcommonData(Model mo, Principal principal) {

		String username = principal.getName();
		System.out.println("username :" + username);
		User user = this.userRepository.getUserByUserName(username);
		System.out.println("User : " + user);
		mo.addAttribute("user", user);
	}

	@GetMapping("/dashboard")
	public String test(Model mo, Principal principal) {

		mo.addAttribute("title", "DashBoard-Smart Contact Manager");
		return "Normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String openAddContactForm(Model mo, Principal principal) {

		mo.addAttribute("title", "Add-Contact-Smart Contact Manager");
		mo.addAttribute("contact", new Contact());
		return "Normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult bindingResult,
			@RequestParam("image") MultipartFile file, Principal principal, HttpSession session) {

		session.setAttribute("session", "hello");
		try {
			String username = principal.getName();
			User user = this.userRepository.getUserByUserName(username);

			if (file.isEmpty()) {
				// if file is empty then try our message
				System.out.println("File is Empty........");
				contact.setImage("contact.jpg");
				// throw new Exception("File should not be empty!!");
			} else {
				// read file and write code for uploading the file

				contact.setImage(file.getOriginalFilename());
				File fsFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(fsFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded.......");
			}
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			System.out.println("Contact :" + contact);
			session.setAttribute("message",
					new Message("Contact has added sucessfully !! Add more....", "alert-success"));
		} catch (Exception e) {

			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
			System.out.println("hello");
			session.setAttribute("message",
					new Message("Something went wrong and try again !! " + e.getMessage(), "alert-danger"));
			System.out.println("hello2");
		}

		return "Normal/add_contact_form";
	}

//	Show Contact Controller
	// per page =5[n]
	// current page=0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model mo, Principal principal) {

		mo.addAttribute("title", "Show Contact-Smart Contact Manager");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUserId(user.getId(), pageable);
		mo.addAttribute("contacts", contacts);
		mo.addAttribute("currentpage", page);
		mo.addAttribute("totalpages", contacts.getTotalPages());
		return "Normal/show_contacts";
	}

	// Showing particular contact details.

	@GetMapping("/{cid}/contact")
	public String showContact(@PathVariable("cid") Integer cid, Model mo, Principal principal) {

		mo.addAttribute("title", "Contact-Details-Smart Contact Manager");

		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		Optional<Contact> contactoptional = this.contactRepository.findById(cid);
		Contact contact = contactoptional.get();
		if (user.getId() == contact.getUser().getId())
			mo.addAttribute("contact", contact);
		System.out.println("cid " + cid);
		return "normal/contact_details";
	}

	// Delete the particular id's contact

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model mo, HttpSession session, Principal principal) {

		Optional<Contact> contactoptional = this.contactRepository.findById(cid);
		Contact contact = contactoptional.get();
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);

		if (user.getId() == contact.getUser().getId()) {
			String imagepath = contact.getImage();
			if (imagepath != null && !imagepath.isEmpty()) {

				try {

					File fsFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(fsFile.getAbsolutePath() + File.separator + imagepath);
					Files.deleteIfExists(path);
					System.out.println("Image Deleted.." + imagepath);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			user.getContacts().removeIf(c -> c.getCid() == contact.getCid());
			this.userRepository.save(user);
			session.setAttribute("message",
					new Message("Contact has been deleted successfully.......", "alert-success"));
		}
		return "redirect:/user/show-contacts/0";
	}

	// update the data

	@PostMapping("/update-contact/{cid}")
	public String updateform(@PathVariable("cid") Integer cid, Model mo) {

		mo.addAttribute("title", "Update-Details-Smart Contact Manager");
		Contact contact = this.contactRepository.findById(cid).get();
		mo.addAttribute("contact", contact);
		return "Normal/update_form";
	}
	
	
	// Process updating in contact

	@PostMapping("/process-update-contact")
	public String processUpdateContactHandler(@ModelAttribute("contact") Contact contact,BindingResult bindingResult,
			@RequestParam("image") MultipartFile file, Model model, Principal principal, HttpSession session) {

		System.out.println("hello contact");
		System.out.println(contact.getName());

		try {

			Contact prevContact  = this.contactRepository.findById(contact.getCid()).get();
			
			String prevImage = prevContact.getImage();

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			if (file.isEmpty()) {
				contact.setImage(prevImage);
			} else {
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file2 = new File(deleteFile, prevImage);
				file2.delete();
	
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact updated successfully ...", "alert-success"));

		} catch (Exception e) {
			System.out.println(e.getMessage());
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
		}

		return "redirect:/user/show-contacts/0";
	}
	
	@GetMapping("/profile")
	public String profile(Model mo) {
		
		mo.addAttribute("title", "DashBoard-Smart Contact Manager");
		return "Normal/profile";
	}
	
	// open setting handler
	@GetMapping("/settings")
	public String openSettings(Model mo) {
		
		mo.addAttribute("title","Setting-Smart Contact Manager");
		return "Normal/settings";
	}
	
	// change password module
	
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		
		System.out.println("old Password: "+oldPassword);
		System.out.println("new Password: "+ newPassword);
		
		String username=principal.getName();
		User cUser=this.userRepository.getUserByUserName(username);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, cUser.getPassword())) {
			
			cUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(cUser);
			session.setAttribute("message", new Message("Your Password has changed successfully ...", "alert-success"));
		}else {
			
			session.setAttribute("message", new Message("Your Old Password has not matched ...","alert-danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/dashboard";
	}

}
