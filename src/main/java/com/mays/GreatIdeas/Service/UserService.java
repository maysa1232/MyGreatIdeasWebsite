package com.mays.GreatIdeas.Service;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.mays.GreatIdeas.Models.User;
import com.mays.GreatIdeas.Repository.UserRepository;

@Service
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// user reg and pass hash..
	public User registerUser(User user) {
		String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

		user.setPassword(hashed);
		return userRepository.save(user);
	}

	// find  by email..
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	// find by id..
	public User findUserById(Long id) {
		Optional<User> u = userRepository.findById(id);

		if (u.isPresent()) {
			return u.get();
		} else {
			return null;
		}
	}

	// authenticate user..
	public boolean authenticateUser(String email, String password) {
		//find by email
		User user = userRepository.findByEmail(email);
		// if not...
		if (user == null) {
			return false;
		} else {
			// if the passwords match, true, else false...
			if (BCrypt.checkpw(password, user.getPassword())) {
				return true;
			} else {
				return false;
			}
		}

	}
}
