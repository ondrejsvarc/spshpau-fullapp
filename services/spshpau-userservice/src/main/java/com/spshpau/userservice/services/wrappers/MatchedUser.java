package com.spshpau.userservice.services.wrappers;

import com.spshpau.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchedUser {
    private User user;
    private double score;
}
