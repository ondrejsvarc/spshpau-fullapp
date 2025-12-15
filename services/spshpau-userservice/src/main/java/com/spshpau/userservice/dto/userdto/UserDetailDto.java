package com.spshpau.userservice.dto.userdto;

import com.spshpau.userservice.dto.profiledto.ArtistProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO for detailed User information, including their full profiles.
 */
@Data
@NoArgsConstructor
public class UserDetailDto {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String location;
    private boolean active;
    private ArtistProfileDetailDto artistProfile;
    private ProducerProfileDetailDto producerProfile;
}
