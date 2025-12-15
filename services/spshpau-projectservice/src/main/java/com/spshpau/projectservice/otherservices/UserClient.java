package com.spshpau.projectservice.otherservices;

import com.spshpau.projectservice.dto.UserSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "userservice", url = "${application.cofig.userclienturl}")
public interface UserClient {
    @GetMapping("/users/search/id/{userId}")
    UserSummaryDto getUserInfoById(@RequestHeader("Authorization") String bearerToken, @PathVariable UUID userId);

    @GetMapping("/interactions/me/connections/all")
    List<UserSummaryDto> findConnectionsByJwt(@RequestHeader("Authorization") String bearerToken);
}
