package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.UserSummaryDto;
import com.spshpau.projectservice.services.exceptions.UserNotFoundException;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.repositories.SimpleUserRepository;
import com.spshpau.projectservice.services.SimpleUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleUserServiceImpl implements SimpleUserService {
    private final SimpleUserRepository simpleUserRepository;

    @Override
    public SimpleUser findUserById(UUID userId) {
        log.info("Attempting to find user by ID: {}", userId);
        SimpleUser user = simpleUserRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
        log.info("User found with ID: {}", userId);
        return user;
    }

    @Override
    @Transactional
    public SimpleUser getOrCreateSimpleUser(UserSummaryDto userSummaryDto) {
        log.info("Attempting to get or create user for UserSummaryDto with ID: {}", userSummaryDto != null ? userSummaryDto.getId() : "null");
        if (userSummaryDto == null || userSummaryDto.getId() == null) {
            log.error("UserSummaryDto or its ID is null.");
            throw new IllegalArgumentException("UserSummaryDto or its ID cannot be null");
        }
        return simpleUserRepository.findById(userSummaryDto.getId())
                .orElseGet(() -> {
                    log.info("User with ID {} not found. Creating new user.", userSummaryDto.getId());
                    SimpleUser newUser = new SimpleUser();
                    newUser.setId(userSummaryDto.getId());
                    newUser.setUsername(userSummaryDto.getUsername());
                    newUser.setFirstName(userSummaryDto.getFirstName());
                    newUser.setLastName(userSummaryDto.getLastName());
                    newUser.setLocation(userSummaryDto.getLocation());
                    SimpleUser savedUser = simpleUserRepository.save(newUser);
                    log.info("New user created and saved with ID: {}", savedUser.getId());
                    return savedUser;
                });
    }

    @Override
    @Transactional
    public SimpleUser getOrCreateSimpleUser(UUID userId, String username, String firstName, String lastName, String location) {
        log.info("Attempting to get or create user with ID: {}", userId);
        if (userId == null) {
            log.error("User ID is null in getOrCreateSimpleUser.");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return simpleUserRepository.findById(userId)
                .orElseGet(() -> {
                    log.info("User with ID {} not found. Creating new user with username: {}", userId, username);
                    SimpleUser newUser = new SimpleUser();
                    newUser.setId(userId);
                    newUser.setUsername(username);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setLocation(location);
                    SimpleUser savedUser = simpleUserRepository.save(newUser);
                    log.info("New user created and saved with ID: {}", savedUser.getId());
                    return savedUser;
                });
    }
}
