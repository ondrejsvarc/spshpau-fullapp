package com.spshpau.userservice.dto.userdto;

import com.spshpau.userservice.model.enums.ExperienceLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class UserSearchCriteria {
    private List<UUID> genreIds; // Filter by users having profiles with ANY of these genres
    private List<UUID> skillIds; // Filter by users having artist profiles with ANY of these skills
    private Boolean hasArtistProfile; // true = must have, false = must NOT have, null = don't filter
    private Boolean hasProducerProfile; // true = must have, false = must NOT have, null = don't filter

    private ExperienceLevel artistExperienceLevel; // Filter by artist experience

    private Boolean artistAvailability; // Filter by artist availability

    private ExperienceLevel producerExperienceLevel; // Filter by producer experience

    private Boolean producerAvailability; // Filter by producer availability

    private String searchTerm; // General text search
}
