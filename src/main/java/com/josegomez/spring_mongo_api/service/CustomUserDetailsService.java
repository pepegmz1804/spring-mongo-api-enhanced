package com.josegomez.spring_mongo_api.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.josegomez.spring_mongo_api.domain.model.Role;
import com.josegomez.spring_mongo_api.domain.model.User;
import com.josegomez.spring_mongo_api.exceptions.ApiException;
import com.josegomez.spring_mongo_api.repository.RoleRepository;
import com.josegomez.spring_mongo_api.repository.UserRepository;
import com.josegomez.spring_mongo_api.security.CustomUserDetails;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User/password not found"));

        if (!user.isEnabled()) {
            throw new ApiException(409, "User disabled");
        }

        List<Role> roles = roleRepository.findByIdIn(user.getRoles());
        return new CustomUserDetails(user, roles);
    }

}
