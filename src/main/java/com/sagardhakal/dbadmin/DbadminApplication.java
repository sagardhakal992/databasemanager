package com.sagardhakal.dbadmin;

import com.sagardhakal.dbadmin.Models.User;
import com.sagardhakal.dbadmin.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
public class DbadminApplication implements CommandLineRunner {

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(DbadminApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Optional<User> userDetails=userService.getAnyUser();
		if(userDetails.isPresent()) {
			System.out.println("User Already Exists");
			return;
		}
		// Take username and password as input
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter username: ");
		String username = scanner.nextLine();

		System.out.print("Enter password: ");
		String password = scanner.nextLine();

		// Encrypt the password
		String encryptedPassword = passwordEncoder.encode(password);

		// Save the user
		userService.createUser(username, encryptedPassword);
		System.out.println("User created successfully!");
	}
}
