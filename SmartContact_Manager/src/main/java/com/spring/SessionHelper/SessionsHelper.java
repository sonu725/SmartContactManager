package com.spring.SessionHelper;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionsHelper {
	
	public void removeMessageFromSession() {
		
		try {
			
			System.out.println("Remove message from session");
			HttpSession session=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
			session.removeAttribute("message");
			
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
