package com.project.soc.service;

import com.project.soc.dto.auth.AuthResponse;
import com.project.soc.dto.auth.LoginRequest;
import com.project.soc.dto.auth.RegisterRequest;
import com.project.soc.entity.User;
import com.project.soc.enums.Role;
import com.project.soc.exception.BadRequestException;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.UserRepository;
import com.project.soc.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DomainMapper domainMapper;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Role role = userRepository.count() == 0 ? Role.ADMIN : Role.ANALYST;
        User user = User.builder()
                .fullName(req.getFullName().trim())
                .email(req.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();
        user = userRepository.save(user);
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(domainMapper.toUserResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .user(domainMapper.toUserResponse(user))
                .build();
    }
}
