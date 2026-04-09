package com.project.soc.service;

import com.project.soc.dto.auth.UserResponse;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DomainMapper domainMapper;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return userRepository.findById(userId)
                .map(domainMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
