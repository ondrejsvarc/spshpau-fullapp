package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.model.SimpleUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String location;

    private ArtistProfileSummaryDto artistProfile;
    private ProducerProfileSummaryDto producerProfile;

    public UserSummaryDto(UUID id, String username, String firstName, String lastName, String location) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
    }

    public static UserSummaryDto fromEntity(SimpleUser simpleUser) {
        if (simpleUser == null) {
            return null;
        }
        return UserSummaryDto.builder()
                .id(simpleUser.getId())
                .username(simpleUser.getUsername())
                .firstName(simpleUser.getFirstName())
                .lastName(simpleUser.getLastName())
                .location(simpleUser.getLocation())
                .artistProfile(null)
                .producerProfile(null)
                .build();
    }
}
