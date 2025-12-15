package com.spshpau.projectservice.services;

import com.spshpau.projectservice.dto.UserSummaryDto;
import com.spshpau.projectservice.model.SimpleUser;

import java.util.UUID;

public interface SimpleUserService {
    SimpleUser findUserById(UUID userId);
    SimpleUser getOrCreateSimpleUser(UserSummaryDto userSummaryDto);
    SimpleUser getOrCreateSimpleUser(UUID userId, String username, String firstName, String lastName, String location);
}
