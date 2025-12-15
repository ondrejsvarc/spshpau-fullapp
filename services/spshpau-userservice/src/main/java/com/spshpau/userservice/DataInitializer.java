package com.spshpau.userservice;

import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.Skill;
import com.spshpau.userservice.repositories.GenreRepository;
import com.spshpau.userservice.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeGenres();
        initializeSkills();
    }

    private void initializeGenres() {
        List<String> genreNames = Arrays.asList(
                "Rock", "Pop", "Hip Hop", "Jazz", "Electronic",
                "Classical", "Blues", "Reggae", "Country", "Metal",
                "Funk", "Soul", "Ambient", "Techno", "House"
        );

        int count = 0;
        for (String name : genreNames) {
            if (genreRepository.findByNameIgnoreCase(name).isEmpty()) {
                Genre newGenre = new Genre(name);
                genreRepository.save(newGenre);
                count++;
            }
        }
    }

    private void initializeSkills() {
        List<String> skillNames = Arrays.asList(
                "Vocals", "Guitar", "Bass Guitar", "Drums", "Keyboards",
                "Songwriting", "Music Production", "Mixing", "Mastering",
                "Turntablism", "Live Sound", "Synth Programming"
        );

        int count = 0;
        for (String name : skillNames) {
            if (skillRepository.findByNameIgnoreCase(name).isEmpty()) {
                Skill newSkill = new Skill(name);
                skillRepository.save(newSkill);
                count++;
            }
        }
    }
}
