package com.talk_space.seed;

import com.talk_space.infrastructure.RedisSupport;
import com.talk_space.model.domain.Hobby;
import com.talk_space.model.domain.Location;
import com.talk_space.model.domain.Speciality;
import com.talk_space.model.domain.User;
import com.talk_space.model.enums.Role;
import com.talk_space.repository.HobbyRepository;
import com.talk_space.repository.LocationRepository;
import com.talk_space.repository.SpecialityRepository;
import com.talk_space.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Fills only blank profile fields of the existing named seed accounts, exactly once per account. */
@Component
@ConditionalOnProperty(prefix="app.demo-seed", name="enabled", havingValue="true", matchIfMissing=true)
public class DemoProfileSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoProfileSeeder.class);
    private final UserRepository users;
    private final HobbyRepository hobbies;
    private final SpecialityRepository specialities;
    private final LocationRepository locations;
    private final ProfileSeedMarkerRepository markers;
    private final RedisSupport redis;

    public DemoProfileSeeder(UserRepository users, HobbyRepository hobbies, SpecialityRepository specialities,
                             LocationRepository locations, ProfileSeedMarkerRepository markers, RedisSupport redis) {
        this.users = users; this.hobbies = hobbies; this.specialities = specialities;
        this.locations = locations; this.markers = markers; this.redis = redis;
    }
    record Profile(String userName, String email, String about, String region, String city,
                   List<String> hobbies, List<String> specialities) {}
    static Profile profile(String name, String email, String about, String region, String city, String[] hobbies, String[] specialities) {
        return new Profile(name,email,about,region,city,List.of(hobbies),List.of(specialities));
    }
    static List<Profile> profiles() {
        return List.of(
            profile("AramH95", "aram.hakob95@gmail.com", "Backend developer who enjoys building useful products, photography and playing guitar. Always interested in good conversations about technology.", "Yerevan", "Yerevan", new String[]{"Photography", "Playing Guitar", "Technical Writing", "Music Production"}, new String[]{"Backend Development", "API Development", "Software Architecture"}),
            profile("MariamP97", "mariam.pet97@gmail.com", "Graphic designer inspired by colors, city walks and photography. I enjoy sharing creative ideas and learning new illustration techniques.", "Yerevan", "Yerevan", new String[]{"Graphic Design", "Photography", "Digital Art", "Illustrating"}, new String[]{"Graphic Designer", "Digital Artist", "Illustrator"}),
            profile("NarekS00", "narek.sar2000@gmail.com", "Frontend developer interested in clean interfaces, music and visual storytelling. I love turning ideas into practical experiences.", "Kotayk", "Abovyan", new String[]{"Digital Art", "Graphic Design", "Playing Guitar", "Photography"}, new String[]{"Frontend Development", "Web Developer", "Full-Stack Development"}),
            profile("AniG96", "ani.ghaz96@gmail.com", "A curious teacher and reader who enjoys creative writing, piano and exchanging ideas about education and personal growth.", "Shirak", "Gyumri", new String[]{"Writing Fiction", "Playing Piano", "Writing Poetry", "Photography"}, new String[]{"Teacher", "Language Teacher", "Online Educator"}),
            profile("GorA93", "gor.ava93@gmail.com", "Engineer by profession, music lover by heart. I spend my free time recording melodies, playing guitar and exploring photography.", "Lori", "Vanadzor", new String[]{"Playing Guitar", "Music Production", "Photography", "Audio Engineering"}, new String[]{"Engineer", "Sound Engineer", "Music Producer"}),
            profile("SiranH98", "siran.har98@gmail.com", "Illustrator and visual storyteller who loves sketching, watercolor and photographing everyday details.", "Yerevan", "Yerevan", new String[]{"Illustrating", "Watercolor Painting", "Sketching", "Photography"}, new String[]{"Illustrator", "Artist", "Digital Artist"}),
            profile("TigranK99", "tigran.khac99@gmail.com", "Software engineer who enjoys solving problems, building APIs and discussing new ideas over music and photography.", "Yerevan", "Yerevan", new String[]{"Photography", "Technical Writing", "Playing Guitar", "Music Production"}, new String[]{"Backend Development", "Software Engineer", "API Development"}),
            profile("LusineH01", "lusine.hov01@gmail.com", "Pianist and music teacher who loves discovering new melodies, writing and meeting other creative people.", "Armavir", "Vagharshapat", new String[]{"Playing Piano", "Music Theory", "Songwriting", "Writing Poetry"}, new String[]{"Pianist", "Music Teacher", "Musician"}),
            profile("KarenM92", "karen.mk92@gmail.com", "Data analyst interested in meaningful patterns, thoughtful storytelling and city photography. Open to meeting people from different fields.", "Yerevan", "Yerevan", new String[]{"Photography", "Technical Writing", "Blogging", "Playing Guitar"}, new String[]{"Data Analyst", "Data Engineering", "Business Data Analyst"}),
            profile("HasmikG94", "hasmik.grig94@gmail.com", "Psychologist who values honest conversations, creative expression and the small details captured through photography.", "Kotayk", "Hrazdan", new String[]{"Photography", "Writing Poetry", "Painting", "Playing Piano"}, new String[]{"Psychologist", "Social Worker", "Teacher"}),
            profile("VahanM90", "vahan.mar90@gmail.com", "Architect fascinated by thoughtful spaces, drawing and photographing buildings. I enjoy learning from people with different creative backgrounds.", "Shirak", "Gyumri", new String[]{"Drawing", "Sketching", "Photography", "Watercolor Painting"}, new String[]{"Architect", "Interior Architect", "Urban Planner"}),
            profile("ElinaS95", "elina.sah95@gmail.com", "UX-minded designer who likes illustrating ideas, exploring photography and making digital art in her free time.", "Yerevan", "Yerevan", new String[]{"Graphic Design", "Illustrating", "Digital Art", "Photography"}, new String[]{"Graphic Designer", "Digital Artist", "Frontend Development"}),
            profile("ArturG91", "artur.gev91@gmail.com", "Teacher who enjoys exploring music, sharing knowledge and writing about everyday life. Happy to connect over common interests.", "Lori", "Vanadzor", new String[]{"Playing Guitar", "Writing Fiction", "Photography", "Music Theory"}, new String[]{"Teacher", "Music Teacher", "Online Educator"}),
            profile("SofyaV96", "sofya.var96@gmail.com", "Content creator and writer interested in visual stories, books and new perspectives. Photography is my favorite way to document a day.", "Ararat", "Artashat", new String[]{"Content Creation", "Blogging", "Photography", "Writing Fiction"}, new String[]{"Content Creator", "Content Writer", "Journalist"}),
            profile("DavidK97", "david.kar97@gmail.com", "Full-stack developer who enjoys practical projects, photography and making music with friends.", "Yerevan", "Yerevan", new String[]{"Photography", "Playing Guitar", "Music Production", "Digital Art"}, new String[]{"Full-Stack Development", "Backend Development", "Frontend Development"}),
            profile("AnahitM98", "anahit.mar98@gmail.com", "Artist and designer inspired by nature, watercolor and handmade details. I enjoy sharing ideas and learning from other creatives.", "Tavush", "Dilijan", new String[]{"Watercolor Painting", "Drawing", "Photography", "Jewelry Making"}, new String[]{"Artist", "Illustrator", "Graphic Designer"}),
            profile("HaykG93", "hayk.ghu93@gmail.com", "Product-minded software engineer interested in backend systems, data and good music. I enjoy collaborating on new ideas.", "Yerevan", "Yerevan", new String[]{"Technical Writing", "Photography", "Playing Guitar", "Music Theory"}, new String[]{"Backend Development", "Data Engineering", "Software Engineer"}),
            profile("ArpiH95", "arpi.hak95@gmail.com", "Photographer and writer who enjoys discovering stories through portraits, creative projects and conversations.", "Armavir", "Vagharshapat", new String[]{"Photography", "Photo Editing", "Writing Fiction", "Blogging"}, new String[]{"Photographer", "Portrait Photographer", "Content Creator"}),
            profile("StepanM92", "stepan.man92@gmail.com", "Musician and audio enthusiast who spends weekends playing piano, making recordings and discovering new sounds.", "Kotayk", "Abovyan", new String[]{"Playing Piano", "Music Production", "Audio Engineering", "Music Composition"}, new String[]{"Musician", "Music Producer", "Sound Engineer"})
        );
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int enriched = 0;
        for (Profile p : profiles()) {
            if (markers.existsById(p.userName())) continue;
            Optional<User> found = users.findUserByUserName(p.userName());
            if (found.isEmpty()) continue;
            User user = found.get();
            // Never change another account that happens to reuse a demo username.
            if (user.getRole() != Role.USER || user.getEmail() == null ||
                    !user.getEmail().equalsIgnoreCase(p.email()) || !Boolean.TRUE.equals(user.getVerifyMail())) continue;
            if (user.getAboutMe() == null || user.getAboutMe().isBlank()) user.setAboutMe(p.about());
            boolean interestsChanged = false;
            if (user.getHobbies() == null || user.getHobbies().isEmpty()) {
                var selected = new ArrayList<Hobby>();
                for (String name : p.hobbies()) hobbies.findHobbyByName(name).ifPresent(selected::add);
                if (!selected.isEmpty()) { user.setHobbies(selected); interestsChanged = true; }
            }
            if (user.getSpecialities() == null || user.getSpecialities().isEmpty()) {
                var selected = new ArrayList<Speciality>();
                for (String name : p.specialities()) specialities.findSpecialityByName(name).ifPresent(selected::add);
                if (!selected.isEmpty()) { user.setSpecialities(selected); interestsChanged = true; }
            }
            if (locations.findByUserUserName(p.userName()).isEmpty()) {
                Location location = new Location();
                location.setCountry("Armenia"); location.setRegion(p.region()); location.setCity(p.city());
                location.setFormattedAddress(p.city() + ", " + p.region() + ", Armenia");
                location.setUser(user); user.setLocation(location);
            }
            users.save(user);
            markers.save(new ProfileSeedMarker(p.userName()));
            if (interestsChanged) redis.evict("matching:" + p.userName());
            enriched++;
        }
        log.info("Initialized profiles for {} existing sample users", enriched);
    }
}
