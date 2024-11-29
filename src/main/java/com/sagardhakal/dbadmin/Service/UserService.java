package com.sagardhakal.dbadmin.Service;

import com.sagardhakal.dbadmin.Models.User;
import com.sagardhakal.dbadmin.Repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createNewUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.getUsersByUsername(username).orElseThrow(()->new UsernameNotFoundException("No User Found For Given Username"));
    }

    public Optional<User> getAnyUser() {
        return userRepository.getAnyUser();
    }

    public void createUser(String username, String encryptedPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptedPassword);
        user.setCreatedDate(new Date());
        userRepository.save(user);
    }
}
