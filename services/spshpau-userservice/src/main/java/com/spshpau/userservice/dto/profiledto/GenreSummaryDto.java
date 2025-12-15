package com.spshpau.userservice.dto.profiledto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * A summary DTO for Genre information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreSummaryDto {
    private UUID id;
    private String name;
}
