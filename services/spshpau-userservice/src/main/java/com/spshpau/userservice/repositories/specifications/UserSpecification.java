package com.spshpau.userservice.repositories.specifications;

import com.spshpau.userservice.dto.userdto.UserSearchCriteria;
import com.spshpau.userservice.model.*;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {

    private final UserSearchCriteria criteria;
    private final UUID currentUserId;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Mandatory Filters: Active and Not Self
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.notEqual(root.get("id"), currentUserId));

        // Optional Filters:

        // --- General Search Term Filter ---
        if (StringUtils.hasText(criteria.getSearchTerm())) {
            String likePattern = "%" + criteria.getSearchTerm().toLowerCase() + "%";
            Predicate usernameMatch = cb.like(cb.lower(root.get("username")), likePattern);
            Predicate firstNameMatch = cb.like(cb.lower(root.get("firstName")), likePattern);
            Predicate lastNameMatch = cb.like(cb.lower(root.get("lastName")), likePattern);
            predicates.add(cb.or(usernameMatch, firstNameMatch, lastNameMatch));
        }

        // --- Profile Existence ---
        handleProfileExistenceFilter(root, cb, predicates, criteria.getHasArtistProfile(), "artistProfile");
        handleProfileExistenceFilter(root, cb, predicates, criteria.getHasProducerProfile(), "producerProfile");

        // --- Profile Attribute Filters ---
        Join<User, ArtistProfile> apJoin = null;
        if (criteria.getArtistExperienceLevel() != null || criteria.getArtistAvailability() != null || !CollectionUtils.isEmpty(criteria.getSkillIds())) {
            apJoin = root.join("artistProfile", JoinType.INNER);
        }

        if (criteria.getArtistExperienceLevel() != null) {
            predicates.add(cb.equal(apJoin.get("experienceLevel"), criteria.getArtistExperienceLevel()));
        }
        if (criteria.getArtistAvailability() != null) {
            predicates.add(cb.equal(apJoin.get("availability"), criteria.getArtistAvailability()));
        }


        Join<User, ProducerProfile> ppJoin = null;
        if (criteria.getProducerExperienceLevel() != null || criteria.getProducerAvailability() != null) {
            ppJoin = root.join("producerProfile", JoinType.INNER);
        }

        if (criteria.getProducerExperienceLevel() != null) {
            predicates.add(cb.equal(ppJoin.get("experienceLevel"), criteria.getProducerExperienceLevel()));
        }
        if (criteria.getProducerAvailability() != null) {
            predicates.add(cb.equal(ppJoin.get("availability"), criteria.getProducerAvailability()));
        }


        // --- Genre Filter ---
        if (!CollectionUtils.isEmpty(criteria.getGenreIds())) {
            query.distinct(true);

            Join<User, ArtistProfile> artistProfileJoinForGenre = root.join("artistProfile", JoinType.LEFT);
            Join<ArtistProfile, Genre> artistGenreJoin = artistProfileJoinForGenre.join("genres", JoinType.LEFT);
            Predicate artistGenrePredicate = cb.and(
                    cb.isNotNull(artistProfileJoinForGenre.get("id")),
                    artistGenreJoin.get("id").in(criteria.getGenreIds())
            );

            Join<User, ProducerProfile> producerProfileJoinForGenre = root.join("producerProfile", JoinType.LEFT);
            Join<ProducerProfile, Genre> producerGenreJoin = producerProfileJoinForGenre.join("genres", JoinType.LEFT);
            Predicate producerGenrePredicate = cb.and(
                    cb.isNotNull(producerProfileJoinForGenre.get("id")),
                    producerGenreJoin.get("id").in(criteria.getGenreIds())
            );

            predicates.add(cb.or(artistGenrePredicate, producerGenrePredicate));
        }

        // --- Skill Filter ---
        if (!CollectionUtils.isEmpty(criteria.getSkillIds())) {
            if (apJoin == null) {
                apJoin = root.join("artistProfile", JoinType.INNER);
            }
            query.distinct(true);
            Join<ArtistProfile, Skill> artistSkillJoin = apJoin.join("skills");
            predicates.add(artistSkillJoin.get("id").in(criteria.getSkillIds()));
        }


        return cb.and(predicates.toArray(new Predicate[0]));
    }

    // Helper method for profile existence filters
    private void handleProfileExistenceFilter(Root<User> root, CriteriaBuilder cb, List<Predicate> predicates, Boolean hasProfile, String profileAttributeName) {
        if (hasProfile != null) {
            if (hasProfile) {
                predicates.add(cb.isNotNull(root.get(profileAttributeName)));
            } else {
                predicates.add(cb.isNull(root.get(profileAttributeName)));
            }
        }
    }
}
