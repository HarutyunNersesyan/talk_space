package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.Hobby;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.HobbyDto;
import com.talk_space.repository.HobbyRepository;
import com.talk_space.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HobbyService {

    private final HobbyRepository hobbyRepository;

    private final UserRepository userRepository;

    public Hobby save(Hobby hobby) {
        return hobbyRepository.save(hobby);
    }

    public Optional<Hobby> getHobbyById(Long id) {
        return hobbyRepository.findById(id);
    }

    public List<Hobby> getAll() {
        return hobbyRepository.findAll();
    }

    public Hobby update(Hobby hobby) {
        return hobbyRepository.save(hobby);
    }

    public Optional<Hobby> getHobbyByName(String hobbyName) {
        return hobbyRepository.findHobbyByName(hobbyName);

    }

    public void deleteHobbyById(Long id) {
        hobbyRepository.deleteById(id);
    }

    public void deleteHobbyByName(String hobbyName) {
        hobbyRepository.deleteHobbyByName(hobbyName);
    }

    public List<Hobby> saveHobbies(List<Hobby> hobbies) {
        return hobbyRepository.saveAll(hobbies);
    }

    public String addHobbyForUser(HobbyDto hobbyDto) {
        if (hobbyDto.getHobbies().size() > 5) {
            throw new IllegalArgumentException("The number of hobbies cannot exceed 5");
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(hobbyDto.getUserName());
        if (optionalUser.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = optionalUser.get();
        user.getHobbies().clear();

        for (int i = 0; i < hobbyDto.getHobbies().size(); i++) {
            user.getHobbies().add(hobbyRepository.findHobbyByName((hobbyDto.getHobbies().get(i)).getName()).get());
        }
        ;
        userRepository.save(user);

        return ("Hobby added successfully.");
    }

    @PostConstruct
    public void fillDb() {
        List<Hobby> hobbies = new ArrayList<>();

        // Top-level categories (parentId = null)
        hobbies.add(new Hobby("WRITING_AND_LITER", null));
        hobbies.add(new Hobby("VISUAL_ARTS", null));
        hobbies.add(new Hobby("CRAFTS", null));
        hobbies.add(new Hobby("MUSIC", null));
        hobbies.add(new Hobby("PERFORMING_ARTS", null));
        hobbies.add(new Hobby("ADVENTURE_SPORTS", null));
        hobbies.add(new Hobby("WATER_SPORTS", null));
        hobbies.add(new Hobby("WINTER_SPORTS", null));
        hobbies.add(new Hobby("LAND_SPORTS", null));
        hobbies.add(new Hobby("TARGET_SPORTS", null));
        hobbies.add(new Hobby("GAMES_AND_COLLECTING", null));
        hobbies.add(new Hobby("NATURE_HOBBIES", null));
        hobbies.add(new Hobby("MISCELLANEOUS", null));
        hobbies.add(new Hobby("TECH_HOBBIES", null));
        hobbies.add(new Hobby("DIY_PROJECTS", null));
        hobbies.add(new Hobby("PHYSICAL_ACTIVITIES", null));
        hobbies.add(new Hobby("SCIENCE_ACTIVITIES", null));
        hobbies.add(new Hobby("LANGUAGE_HOBBIES", null));
        hobbies.add(new Hobby("AERIAL_ARTS", null)); // New category 19
        hobbies.add(new Hobby("MINDFULNESS", null)); // New category 20
        hobbies.add(new Hobby("TRANSPORTATION", null)); // New category 21
        hobbies.add(new Hobby("FASHION", null)); // New category 22
        hobbies.add(new Hobby("HOME_ECONOMICS", null)); // New category 23
        hobbies.add(new Hobby("SOCIAL_DANCES", null)); // New category 24
        hobbies.add(new Hobby("HISTORY_REENACTMENT", null)); // New category 25
        hobbies.add(new Hobby("ASTRONOMY_OBSERVATION", null)); // New category 26
        hobbies.add(new Hobby("PUZZLES", null)); // New category 27
        hobbies.add(new Hobby("EXTREME_SPORTS", null)); // New category 28
        hobbies.add(new Hobby("COOKING_TECHNIQUES", null)); // New category 29
        hobbies.add(new Hobby("ANIMAL_CARE", null)); // New category 30
        hobbies.add(new Hobby("URBAN_EXPLORATION", null)); // New category 31
        hobbies.add(new Hobby("GREEN_TECH", null)); // New category 32
        hobbies.add(new Hobby("MARTIAL_ARTS", null)); // New category 33
        hobbies.add(new Hobby("COLLECTIBLES", null)); // New category 34
        hobbies.add(new Hobby("BOARD_SPORTS", null)); // New category 35
        hobbies.add(new Hobby("WATER_ACTIVITIES", null)); // New category 36
        hobbies.add(new Hobby("MIND_SPORTS", null)); // New category 37
        hobbies.add(new Hobby("FIBER_ARTS", null)); // New category 38
        hobbies.add(new Hobby("HOME_BREWING", null)); // New category 39
        hobbies.add(new Hobby("GEOCACHING", null)); // New category 40
        hobbies.add(new Hobby("COSPLAY", null)); // New category 41
        hobbies.add(new Hobby("VINTAGE_CARS", null)); // New category 42
        hobbies.add(new Hobby("AQUARIUMS", null)); // New category 43
        hobbies.add(new Hobby("BONSAI", null)); // New category 44
        hobbies.add(new Hobby("AMATEUR_RADIO", null)); // New category 45
        hobbies.add(new Hobby("LOCK_PICKING", null)); // New category 46
        hobbies.add(new Hobby("ESCAPOLOGY", null)); // New category 47
        hobbies.add(new Hobby("STARGAZING", null)); // New category 48

// Writing and Literature subcategories (parentId = 1)
        hobbies.add(new Hobby("Writing Fiction", 1L));
        hobbies.add(new Hobby("Writing Poetry", 1L));
        hobbies.add(new Hobby("Writing Nonfiction", 1L));
        hobbies.add(new Hobby("Essay Writing", 1L));
        hobbies.add(new Hobby("Blogging", 1L));
        hobbies.add(new Hobby("Vlogging", 1L));
        hobbies.add(new Hobby("Screenwriting", 1L));
        hobbies.add(new Hobby("Technical Writing", 1L));
        hobbies.add(new Hobby("Freelance Writing", 1L));
        hobbies.add(new Hobby("Novel Writing", 1L));
        hobbies.add(new Hobby("Editing", 1L));
        hobbies.add(new Hobby("Proofreading", 1L));
        hobbies.add(new Hobby("Fan Fiction Writing", 1L));
        hobbies.add(new Hobby("Journalism", 1L));
        hobbies.add(new Hobby("Writing Reviews", 1L));
        hobbies.add(new Hobby("Comic Book Writing", 1L));
        hobbies.add(new Hobby("Speech Writing", 1L));
        hobbies.add(new Hobby("Content Creation", 1L));
        hobbies.add(new Hobby("Ghostwriting", 1L));
        hobbies.add(new Hobby("Copywriting", 1L));

// Visual Arts subcategories (parentId = 2)
        hobbies.add(new Hobby("Drawing", 2L));
        hobbies.add(new Hobby("Painting", 2L));
        hobbies.add(new Hobby("Sketching", 2L));
        hobbies.add(new Hobby("Watercolor Painting", 2L));
        hobbies.add(new Hobby("Oil Painting", 2L));
        hobbies.add(new Hobby("Charcoal Drawing", 2L));
        hobbies.add(new Hobby("Digital Art", 2L));
        hobbies.add(new Hobby("Graphic Design", 2L));
        hobbies.add(new Hobby("Comic Art", 2L));
        hobbies.add(new Hobby("Illustrating", 2L));
        hobbies.add(new Hobby("Photography", 2L));
        hobbies.add(new Hobby("Photo Editing", 2L));
        hobbies.add(new Hobby("Videography", 2L));
        hobbies.add(new Hobby("Sculpting", 2L));
        hobbies.add(new Hobby("Ceramics", 2L));
        hobbies.add(new Hobby("Collage Making", 2L));
        hobbies.add(new Hobby("Mural Painting", 2L));
        hobbies.add(new Hobby("Tattoo Design", 2L));
        hobbies.add(new Hobby("Airbrushing", 2L));
        hobbies.add(new Hobby("Calligraphy", 2L));

// Crafts subcategories (parentId = 3)
        hobbies.add(new Hobby("Knitting", 3L));
        hobbies.add(new Hobby("Crocheting", 3L));
        hobbies.add(new Hobby("Embroidery", 3L));
        hobbies.add(new Hobby("Quilting", 3L));
        hobbies.add(new Hobby("Sewing", 3L));
        hobbies.add(new Hobby("Scrapbooking", 3L));
        hobbies.add(new Hobby("Jewelry Making", 3L));
        hobbies.add(new Hobby("Soap Making", 3L));
        hobbies.add(new Hobby("Candle Making", 3L));
        hobbies.add(new Hobby("Origami", 3L));
        hobbies.add(new Hobby("Wood Carving", 3L));
        hobbies.add(new Hobby("Glassblowing", 3L));
        hobbies.add(new Hobby("Basket Weaving", 3L));
        hobbies.add(new Hobby("Leatherworking", 3L));
        hobbies.add(new Hobby("Beadwork", 3L));
        hobbies.add(new Hobby("Papercraft", 3L));
        hobbies.add(new Hobby("Card Making", 3L));
        hobbies.add(new Hobby("Pottery", 3L));
        hobbies.add(new Hobby("Stained Glass", 3L));
        hobbies.add(new Hobby("Macramé", 3L));

// Music subcategories (parentId = 4)
        hobbies.add(new Hobby("Playing Guitar", 4L));
        hobbies.add(new Hobby("Playing Piano", 4L));
        hobbies.add(new Hobby("Playing Violin", 4L));
        hobbies.add(new Hobby("Playing Drums", 4L));
        hobbies.add(new Hobby("Playing Saxophone", 4L));
        hobbies.add(new Hobby("Playing Flute", 4L));
        hobbies.add(new Hobby("Singing", 4L));
        hobbies.add(new Hobby("Songwriting", 4L));
        hobbies.add(new Hobby("Music Production", 4L));
        hobbies.add(new Hobby("DJing", 4L));
        hobbies.add(new Hobby("Music Composition", 4L));
        hobbies.add(new Hobby("Audio Engineering", 4L));
        hobbies.add(new Hobby("Beatboxing", 4L));
        hobbies.add(new Hobby("Harmonica Playing", 4L));
        hobbies.add(new Hobby("Ukulele Playing", 4L));
        hobbies.add(new Hobby("Music Mixing", 4L));
        hobbies.add(new Hobby("Playing Trumpet", 4L));
        hobbies.add(new Hobby("Playing Cello", 4L));
        hobbies.add(new Hobby("Conducting", 4L));
        hobbies.add(new Hobby("Music Theory", 4L));

// Performing Arts subcategories (parentId = 5)
        hobbies.add(new Hobby("Acting", 5L));
        hobbies.add(new Hobby("Stand-up Comedy", 5L));
        hobbies.add(new Hobby("Dancing", 5L));
        hobbies.add(new Hobby("Ballet", 5L));
        hobbies.add(new Hobby("Tap Dancing", 5L));
        hobbies.add(new Hobby("Hip-Hop Dancing", 5L));
        hobbies.add(new Hobby("Street Dancing", 5L));
        hobbies.add(new Hobby("Magic Tricks", 5L));
        hobbies.add(new Hobby("Clowning", 5L));
        hobbies.add(new Hobby("Puppetry", 5L));
        hobbies.add(new Hobby("Mime", 5L));
        hobbies.add(new Hobby("Voice Acting", 5L));
        hobbies.add(new Hobby("Improv Comedy", 5L));
        hobbies.add(new Hobby("Stage Combat", 5L));
        hobbies.add(new Hobby("Stage Production", 5L));
        hobbies.add(new Hobby("Theatre Directing", 5L));
        hobbies.add(new Hobby("Circus Arts", 5L));
        hobbies.add(new Hobby("Juggling", 5L));
        hobbies.add(new Hobby("Fire Dancing", 5L));
        hobbies.add(new Hobby("Acrobats", 5L));

// Adventure Sports subcategories (parentId = 6)
        hobbies.add(new Hobby("Rock Climbing", 6L));
        hobbies.add(new Hobby("Bungee Jumping", 6L));
        hobbies.add(new Hobby("Skydiving", 6L));
        hobbies.add(new Hobby("Paragliding", 6L));
        hobbies.add(new Hobby("Zip Lining", 6L));
        hobbies.add(new Hobby("Mountaineering", 6L));
        hobbies.add(new Hobby("Caving", 6L));
        hobbies.add(new Hobby("Whitewater Rafting", 6L));
        hobbies.add(new Hobby("Mountain Biking", 6L));
        hobbies.add(new Hobby("Off-Roading", 6L));
        hobbies.add(new Hobby("Hang Gliding", 6L));
        hobbies.add(new Hobby("Sandboarding", 6L));
        hobbies.add(new Hobby("Windsurfing", 6L));
        hobbies.add(new Hobby("Parachuting", 6L));
        hobbies.add(new Hobby("Base Jumping", 6L));
        hobbies.add(new Hobby("Cliff Diving", 6L));
        hobbies.add(new Hobby("Free Running", 6L));
        hobbies.add(new Hobby("Highlining", 6L));
        hobbies.add(new Hobby("Ice Climbing", 6L));
        hobbies.add(new Hobby("Dirt Biking", 6L));

// Water Sports subcategories (parentId = 7)
        hobbies.add(new Hobby("Surfing", 7L));
        hobbies.add(new Hobby("Scuba Diving", 7L));
        hobbies.add(new Hobby("Snorkeling", 7L));
        hobbies.add(new Hobby("Kayaking", 7L));
        hobbies.add(new Hobby("Canoeing", 7L));
        hobbies.add(new Hobby("Paddleboarding", 7L));
        hobbies.add(new Hobby("Sailing", 7L));
        hobbies.add(new Hobby("Water Skiing", 7L));
        hobbies.add(new Hobby("Jet Skiing", 7L));
        hobbies.add(new Hobby("Swimming", 7L));
        hobbies.add(new Hobby("Fishing", 7L));
        hobbies.add(new Hobby("Rowing", 7L));
        hobbies.add(new Hobby("Kite Surfing", 7L));
        hobbies.add(new Hobby("Bodyboarding", 7L));
        hobbies.add(new Hobby("Deep Sea Fishing", 7L));
        hobbies.add(new Hobby("Wakeboarding", 7L));
        hobbies.add(new Hobby("Rafting", 7L));
        hobbies.add(new Hobby("Spearfishing", 7L));
        hobbies.add(new Hobby("Freediving", 7L));

// Winter Sports subcategories (parentId = 8)
        hobbies.add(new Hobby("Skiing", 8L));
        hobbies.add(new Hobby("Snowboarding", 8L));
        hobbies.add(new Hobby("Ice Skating", 8L));
        hobbies.add(new Hobby("Ice Hockey", 8L));
        hobbies.add(new Hobby("Snowshoeing", 8L));
        hobbies.add(new Hobby("Snowmobiling", 8L));
        hobbies.add(new Hobby("Curling", 8L));
        hobbies.add(new Hobby("Snow Tubing", 8L));
        hobbies.add(new Hobby("Luge", 8L));
        hobbies.add(new Hobby("Bobsledding", 8L));
        hobbies.add(new Hobby("Snow Kiting", 8L));
        hobbies.add(new Hobby("Sledding", 8L));
        hobbies.add(new Hobby("Cross-country Skiing", 8L));
        hobbies.add(new Hobby("Snowball Fighting", 8L));
        hobbies.add(new Hobby("Winter Camping", 8L));
        hobbies.add(new Hobby("Ice Fishing", 8L));
        hobbies.add(new Hobby("Dog Sledding", 8L));
        hobbies.add(new Hobby("Ski Jumping", 8L));

// Land Sports subcategories (parentId = 9)
        hobbies.add(new Hobby("Football", 9L));
        hobbies.add(new Hobby("Basketball", 9L));
        hobbies.add(new Hobby("Baseball", 9L));
        hobbies.add(new Hobby("American Football", 9L));
        hobbies.add(new Hobby("Rugby", 9L));
        hobbies.add(new Hobby("Cricket", 9L));
        hobbies.add(new Hobby("Tennis", 9L));
        hobbies.add(new Hobby("Badminton", 9L));
        hobbies.add(new Hobby("Volleyball", 9L));
        hobbies.add(new Hobby("Golf", 9L));
        hobbies.add(new Hobby("Track and Field", 9L));
        hobbies.add(new Hobby("CrossFit", 9L));
        hobbies.add(new Hobby("Parkour", 9L));
        hobbies.add(new Hobby("Lacrosse", 9L));
        hobbies.add(new Hobby("Handball", 9L));
        hobbies.add(new Hobby("Table Tennis", 9L));
        hobbies.add(new Hobby("Field Hockey", 9L));
        hobbies.add(new Hobby("Dodgeball", 9L));
        hobbies.add(new Hobby("Ultimate Frisbee", 9L));
        hobbies.add(new Hobby("Horseback Riding", 9L));

// Target Sports subcategories (parentId = 10)
        hobbies.add(new Hobby("Archery", 10L));
        hobbies.add(new Hobby("Airsoft", 10L));
        hobbies.add(new Hobby("Paintball", 10L));
        hobbies.add(new Hobby("Skeet Shooting", 10L));
        hobbies.add(new Hobby("Trap Shooting", 10L));
        hobbies.add(new Hobby("Pistol Shooting", 10L));
        hobbies.add(new Hobby("Rifle Shooting", 10L));
        hobbies.add(new Hobby("Dart Throwing", 10L));
        hobbies.add(new Hobby("Axe Throwing", 10L));
        hobbies.add(new Hobby("Slingshot Shooting", 10L));
        hobbies.add(new Hobby("Clay Pigeon Shooting", 10L));
        hobbies.add(new Hobby("Bow Hunting", 10L));
        hobbies.add(new Hobby("Crossbow Shooting", 10L));
        hobbies.add(new Hobby("Laser Tag", 10L));
        hobbies.add(new Hobby("Knife Throwing", 10L));
        hobbies.add(new Hobby("Sporting Clays", 10L));
        hobbies.add(new Hobby("Biathlon", 10L));
        hobbies.add(new Hobby("Field Archery", 10L));
        hobbies.add(new Hobby("Combat Archery", 10L));
        hobbies.add(new Hobby("Precision Shooting", 10L));

// Games and Collecting subcategories (parentId = 11)
        hobbies.add(new Hobby("Chess", 11L));
        hobbies.add(new Hobby("Checkers", 11L));
        hobbies.add(new Hobby("Poker", 11L));
        hobbies.add(new Hobby("Bridge", 11L));
        hobbies.add(new Hobby("Backgammon", 11L));
        hobbies.add(new Hobby("Scrabble", 11L));
        hobbies.add(new Hobby("Dungeons and Dragons", 11L));
        hobbies.add(new Hobby("Magic: The Gathering", 11L));
        hobbies.add(new Hobby("Board Games", 11L));
        hobbies.add(new Hobby("Card Collecting", 11L));
        hobbies.add(new Hobby("Coin Collecting", 11L));
        hobbies.add(new Hobby("Stamp Collecting", 11L));
        hobbies.add(new Hobby("Antique Collecting", 11L));
        hobbies.add(new Hobby("Action Figure Collecting", 11L));
        hobbies.add(new Hobby("Comic Book Collecting", 11L));
        hobbies.add(new Hobby("Model Train Building", 11L));
        hobbies.add(new Hobby("Puzzle Solving", 11L));
        hobbies.add(new Hobby("Fantasy Football", 11L));
        hobbies.add(new Hobby("Cosplay", 11L));
        hobbies.add(new Hobby("Video Gaming", 11L));

// Nature Hobbies subcategories (parentId = 12)
        hobbies.add(new Hobby("Bird Watching", 12L));
        hobbies.add(new Hobby("Gardening", 12L));
        hobbies.add(new Hobby("Beekeeping", 12L));
        hobbies.add(new Hobby("Butterfly Watching", 12L));
        hobbies.add(new Hobby("Hiking", 12L));
        hobbies.add(new Hobby("Camping", 12L));
        hobbies.add(new Hobby("Nature Photography", 12L));
        hobbies.add(new Hobby("Forest Bathing", 12L));
        hobbies.add(new Hobby("Tree Climbing", 12L));
        hobbies.add(new Hobby("Plant Identification", 12L));
        hobbies.add(new Hobby("Animal Tracking", 12L));
        hobbies.add(new Hobby("Mushroom Hunting", 12L));
        hobbies.add(new Hobby("Stargazing", 12L));
        hobbies.add(new Hobby("Geocaching", 12L));
        hobbies.add(new Hobby("Fossil Hunting", 12L));
        hobbies.add(new Hobby("Rock Collecting", 12L));
        hobbies.add(new Hobby("Wildlife Conservation", 12L));
        hobbies.add(new Hobby("Flower Arranging", 12L));
        hobbies.add(new Hobby("Foraging", 12L));
        hobbies.add(new Hobby("Herb Gardening", 12L));

// Miscellaneous subcategories (parentId = 13)
        hobbies.add(new Hobby("Traveling", 13L));
        hobbies.add(new Hobby("Volunteering", 13L));
        hobbies.add(new Hobby("Meditation", 13L));
        hobbies.add(new Hobby("Yoga", 13L));
        hobbies.add(new Hobby("Dog Training", 13L));
        hobbies.add(new Hobby("Cat Care", 13L));
        hobbies.add(new Hobby("Pet Sitting", 13L));
        hobbies.add(new Hobby("Cooking", 13L));
        hobbies.add(new Hobby("Baking", 13L));
        hobbies.add(new Hobby("Wine Tasting", 13L));
        hobbies.add(new Hobby("Cheese Making", 13L));
        hobbies.add(new Hobby("Mixology", 13L));
        hobbies.add(new Hobby("Interior Decorating", 13L));
        hobbies.add(new Hobby("Personal Shopping", 13L));
        hobbies.add(new Hobby("Fashion Styling", 13L));
        hobbies.add(new Hobby("Event Planning", 13L));
        hobbies.add(new Hobby("Social Media Management", 13L));
        hobbies.add(new Hobby("Car Collecting", 13L));
        hobbies.add(new Hobby("Antique Restoration", 13L));
        hobbies.add(new Hobby("Bartending", 13L));

// Tech Hobbies subcategories (parentId = 14)
        hobbies.add(new Hobby("Coding", 14L));
        hobbies.add(new Hobby("Game Development", 14L));
        hobbies.add(new Hobby("App Development", 14L));
        hobbies.add(new Hobby("Web Development", 14L));
        hobbies.add(new Hobby("Software Testing", 14L));
        hobbies.add(new Hobby("Cybersecurity", 14L));
        hobbies.add(new Hobby("Ethical Hacking", 14L));
        hobbies.add(new Hobby("Arduino Programming", 14L));
        hobbies.add(new Hobby("Raspberry Pi Projects", 14L));
        hobbies.add(new Hobby("3D Printing", 14L));
        hobbies.add(new Hobby("Drone Piloting", 14L));
        hobbies.add(new Hobby("Electronics Repair", 14L));
        hobbies.add(new Hobby("Computer Building", 14L));
        hobbies.add(new Hobby("Virtual Reality Design", 14L));
        hobbies.add(new Hobby("Augmented Reality Design", 14L));
        hobbies.add(new Hobby("Robotics", 14L));
        hobbies.add(new Hobby("Machine Learning", 14L));
        hobbies.add(new Hobby("Artificial Intelligence", 14L));
        hobbies.add(new Hobby("Data Science", 14L));
        hobbies.add(new Hobby("Quantum Computing", 14L));

// DIY Projects subcategories (parentId = 15)
        hobbies.add(new Hobby("Home Improvement", 15L));
        hobbies.add(new Hobby("Furniture Making", 15L));
        hobbies.add(new Hobby("Carpentry", 15L));
        hobbies.add(new Hobby("Electrical Work", 15L));
        hobbies.add(new Hobby("Plumbing", 15L));
        hobbies.add(new Hobby("Welding", 15L));
        hobbies.add(new Hobby("Metalworking", 15L));
        hobbies.add(new Hobby("Model Building", 15L));
        hobbies.add(new Hobby("Car Restoration", 15L));
        hobbies.add(new Hobby("Bicycle Repair", 15L));
        hobbies.add(new Hobby("Masonry", 15L));
        hobbies.add(new Hobby("Gardening Structures", 15L));
        hobbies.add(new Hobby("Home Automation", 15L));
        hobbies.add(new Hobby("Solar Panel Installation", 15L));
        hobbies.add(new Hobby("Painting Walls", 15L));
        hobbies.add(new Hobby("Tiling", 15L));
        hobbies.add(new Hobby("Flooring Installation", 15L));
        hobbies.add(new Hobby("Window Repair", 15L));
        hobbies.add(new Hobby("Roof Repair", 15L));
        hobbies.add(new Hobby("Insulation Installation", 15L));

// Physical Activities subcategories (parentId = 16)
        hobbies.add(new Hobby("Running", 16L));
        hobbies.add(new Hobby("Walking", 16L));
        hobbies.add(new Hobby("Jogging", 16L));
        hobbies.add(new Hobby("Weightlifting", 16L));
        hobbies.add(new Hobby("Aerobics", 16L));
        hobbies.add(new Hobby("Pilates", 16L));
        hobbies.add(new Hobby("Spinning", 16L));
        hobbies.add(new Hobby("Zumba", 16L));
        hobbies.add(new Hobby("Martial Arts", 16L));
        hobbies.add(new Hobby("Boxing", 16L));
        hobbies.add(new Hobby("Kickboxing", 16L));
        hobbies.add(new Hobby("Jiu-Jitsu", 16L));
        hobbies.add(new Hobby("Karate", 16L));
        hobbies.add(new Hobby("Taekwondo", 16L));
        hobbies.add(new Hobby("Capoeira", 16L));
        hobbies.add(new Hobby("Krav Maga", 16L));
        hobbies.add(new Hobby("Powerlifting", 16L));
        hobbies.add(new Hobby("Bodybuilding", 16L));
        hobbies.add(new Hobby("Wrestling", 16L));

// Science Activities subcategories (parentId = 17)
        hobbies.add(new Hobby("Astronomy", 17L));
        hobbies.add(new Hobby("Chemistry Experiments", 17L));
        hobbies.add(new Hobby("Physics Projects", 17L));
        hobbies.add(new Hobby("Electronics Projects", 17L));
        hobbies.add(new Hobby("Space Exploration", 17L));
        hobbies.add(new Hobby("Geology", 17L));
        hobbies.add(new Hobby("Meteorology", 17L));
        hobbies.add(new Hobby("Biology Research", 17L));
        hobbies.add(new Hobby("Environmental Science", 17L));
        hobbies.add(new Hobby("Amateur Meteorology", 17L));
        hobbies.add(new Hobby("Marine Biology", 17L));
        hobbies.add(new Hobby("Botany", 17L));
        hobbies.add(new Hobby("Paleontology", 17L));
        hobbies.add(new Hobby("Zoology", 17L));
        hobbies.add(new Hobby("DNA Testing", 17L));
        hobbies.add(new Hobby("Genealogy", 17L));
        hobbies.add(new Hobby("Weather Forecasting", 17L));
        hobbies.add(new Hobby("Natural History Research", 17L));

// Language Hobbies subcategories (parentId = 18)
        hobbies.add(new Hobby("Language Learning", 18L));
        hobbies.add(new Hobby("Conlanging", 18L));
        hobbies.add(new Hobby("Translation", 18L));
        hobbies.add(new Hobby("Sign Language", 18L));
        hobbies.add(new Hobby("Linguistics", 18L));

// Aerial Arts subcategories (parentId = 19)
        hobbies.add(new Hobby("Aerial Silks", 19L));
        hobbies.add(new Hobby("Aerial Hoop", 19L));
        hobbies.add(new Hobby("Pole Dancing", 19L));
        hobbies.add(new Hobby("Trapeze", 19L));
        hobbies.add(new Hobby("Aerial Rope", 19L));
        hobbies.add(new Hobby("Aerial Hammock", 19L));
        hobbies.add(new Hobby("Lyra", 19L));
        hobbies.add(new Hobby("Cloud Swing", 19L));
        hobbies.add(new Hobby("Spanish Web", 19L));
        hobbies.add(new Hobby("Corde Lisse", 19L));

// Mindfulness subcategories (parentId = 20)
        hobbies.add(new Hobby("Guided Meditation", 20L));
        hobbies.add(new Hobby("Breathwork", 20L));
        hobbies.add(new Hobby("Sound Baths", 20L));
        hobbies.add(new Hobby("Forest Therapy", 20L));
        hobbies.add(new Hobby("Gratitude Journaling", 20L));
        hobbies.add(new Hobby("Mindful Walking", 20L));
        hobbies.add(new Hobby("Body Scanning", 20L));
        hobbies.add(new Hobby("Loving-Kindness Meditation", 20L));
        hobbies.add(new Hobby("Zen Meditation", 20L));
        hobbies.add(new Hobby("Chakra Balancing", 20L));

// Transportation subcategories (parentId = 21)
        hobbies.add(new Hobby("Bicycle Touring", 21L));
        hobbies.add(new Hobby("Motorcycle Riding", 21L));
        hobbies.add(new Hobby("Sailing Voyages", 21L));
        hobbies.add(new Hobby("RV Travel", 21L));
        hobbies.add(new Hobby("Train Spotting", 21L));
        hobbies.add(new Hobby("Aircraft Spotting", 21L));
        hobbies.add(new Hobby("Hot Air Ballooning", 21L));
        hobbies.add(new Hobby("Segway Touring", 21L));
        hobbies.add(new Hobby("Horseback Travel", 21L));
        hobbies.add(new Hobby("Canoe Camping", 21L));

// Fashion subcategories (parentId = 22)
        hobbies.add(new Hobby("Sewing Clothes", 22L));
        hobbies.add(new Hobby("Fashion Design", 22L));
        hobbies.add(new Hobby("Upcycling Clothes", 22L));
        hobbies.add(new Hobby("Shoe Making", 22L));
        hobbies.add(new Hobby("Hat Making", 22L));
        hobbies.add(new Hobby("Jewelry Design", 22L));
        hobbies.add(new Hobby("Handbag Design", 22L));
        hobbies.add(new Hobby("Fashion Photography", 22L));
        hobbies.add(new Hobby("Wardrobe Styling", 22L));
        hobbies.add(new Hobby("Fabric Dyeing", 22L));

// Home Economics subcategories (parentId = 23)
        hobbies.add(new Hobby("Meal Planning", 23L));
        hobbies.add(new Hobby("Budgeting", 23L));
        hobbies.add(new Hobby("Home Organization", 23L));
        hobbies.add(new Hobby("Minimalism", 23L));
        hobbies.add(new Hobby("Zero Waste Living", 23L));
        hobbies.add(new Hobby("Sustainable Living", 23L));
        hobbies.add(new Hobby("Home Canning", 23L));
        hobbies.add(new Hobby("Fermentation", 23L));
        hobbies.add(new Hobby("Home Brewing", 23L));
        hobbies.add(new Hobby("Preserving Food", 23L));

// Social Dances subcategories (parentId = 24)
        hobbies.add(new Hobby("Swing Dancing", 24L));
        hobbies.add(new Hobby("Salsa Dancing", 24L));
        hobbies.add(new Hobby("Tango Dancing", 24L));
        hobbies.add(new Hobby("Ballroom Dancing", 24L));
        hobbies.add(new Hobby("Square Dancing", 24L));
        hobbies.add(new Hobby("Line Dancing", 24L));
        hobbies.add(new Hobby("Folk Dancing", 24L));
        hobbies.add(new Hobby("Country Western Dancing", 24L));
        hobbies.add(new Hobby("Belly Dancing", 24L));
        hobbies.add(new Hobby("Irish Step Dancing", 24L));

// History Reenactment subcategories (parentId = 25)
        hobbies.add(new Hobby("Medieval Reenactment", 25L));
        hobbies.add(new Hobby("Civil War Reenactment", 25L));
        hobbies.add(new Hobby("Viking Reenactment", 25L));
        hobbies.add(new Hobby("Renaissance Fairs", 25L));
        hobbies.add(new Hobby("Ancient Roman Reenactment", 25L));
        hobbies.add(new Hobby("Wild West Reenactment", 25L));
        hobbies.add(new Hobby("World War II Reenactment", 25L));
        hobbies.add(new Hobby("Historical Fencing", 25L));
        hobbies.add(new Hobby("Traditional Archery", 25L));
        hobbies.add(new Hobby("Blacksmithing", 25L));

// Astronomy Observation subcategories (parentId = 26)
        hobbies.add(new Hobby("Solar Observation", 26L));
        hobbies.add(new Hobby("Lunar Observation", 26L));
        hobbies.add(new Hobby("Planetary Observation", 26L));
        hobbies.add(new Hobby("Deep Sky Observation", 26L));
        hobbies.add(new Hobby("Meteor Watching", 26L));
        hobbies.add(new Hobby("Comet Hunting", 26L));
        hobbies.add(new Hobby("Aurora Watching", 26L));
        hobbies.add(new Hobby("Satellite Tracking", 26L));
        hobbies.add(new Hobby("Eclipse Chasing", 26L));
        hobbies.add(new Hobby("Astrophotography", 26L));

// Puzzles subcategories (parentId = 27)
        hobbies.add(new Hobby("Crossword Puzzles", 27L));
        hobbies.add(new Hobby("Sudoku", 27L));
        hobbies.add(new Hobby("Jigsaw Puzzles", 27L));
        hobbies.add(new Hobby("Logic Puzzles", 27L));
        hobbies.add(new Hobby("Escape Rooms", 27L));
        hobbies.add(new Hobby("Brain Teasers", 27L));
        hobbies.add(new Hobby("Rubik's Cube", 27L));
        hobbies.add(new Hobby("Cryptic Puzzles", 27L));
        hobbies.add(new Hobby("Puzzle Hunts", 27L));
        hobbies.add(new Hobby("Mechanical Puzzles", 27L));

// Extreme Sports subcategories (parentId = 28)
        hobbies.add(new Hobby("Wingsuit Flying", 28L));
        hobbies.add(new Hobby("Cave Diving", 28L));
        hobbies.add(new Hobby("Big Wave Surfing", 28L));
        hobbies.add(new Hobby("Volcano Boarding", 28L));
        hobbies.add(new Hobby("Ice Cross Downhill", 28L));
        hobbies.add(new Hobby("Street Luge", 28L));
        hobbies.add(new Hobby("Freestyle Motocross", 28L));
        hobbies.add(new Hobby("Whitewater Kayaking", 28L));
        hobbies.add(new Hobby("Mountain Unicycling", 28L));
        hobbies.add(new Hobby("Ultra Running", 28L));
        hobbies.add(new Hobby("Sous Vide Cooking", 29L));
        hobbies.add(new Hobby("Molecular Gastronomy", 29L));
        hobbies.add(new Hobby("Fermentation", 29L));
        hobbies.add(new Hobby("Smoking Foods", 29L));
        hobbies.add(new Hobby("Butchery", 29L));
        hobbies.add(new Hobby("Charcuterie", 29L));
        hobbies.add(new Hobby("Artisan Bread Making", 29L));
        hobbies.add(new Hobby("Pasta Making", 29L));
        hobbies.add(new Hobby("Chocolate Tempering", 29L));
        hobbies.add(new Hobby("Sugar Art", 29L));

// Animal Care subcategories (parentId = 30)
        hobbies.add(new Hobby("Dog Breeding", 30L));
        hobbies.add(new Hobby("Horse Training", 30L));
        hobbies.add(new Hobby("Falconry", 30L));
        hobbies.add(new Hobby("Beekeeping", 30L));
        hobbies.add(new Hobby("Butterfly Rearing", 30L));
        hobbies.add(new Hobby("Aquarium Keeping", 30L));
        hobbies.add(new Hobby("Reptile Care", 30L));
        hobbies.add(new Hobby("Bird Watching", 30L));
        hobbies.add(new Hobby("Animal Rescue", 30L));
        hobbies.add(new Hobby("Pet Grooming", 30L));

// Urban Exploration subcategories (parentId = 31)
        hobbies.add(new Hobby("Abandoned Buildings", 31L));
        hobbies.add(new Hobby("Drainage Tunnels", 31L));
        hobbies.add(new Hobby("Rooftopping", 31L));
        hobbies.add(new Hobby("Catacombs", 31L));
        hobbies.add(new Hobby("Industrial Sites", 31L));
        hobbies.add(new Hobby("Ghost Towns", 31L));
        hobbies.add(new Hobby("Underground Bunkers", 31L));
        hobbies.add(new Hobby("Bridge Climbing", 31L));
        hobbies.add(new Hobby("Urban Caving", 31L));
        hobbies.add(new Hobby("Subway Tunnels", 31L));

// Green Tech subcategories (parentId = 32)
        hobbies.add(new Hobby("Solar Power DIY", 32L));
        hobbies.add(new Hobby("Wind Turbine Building", 32L));
        hobbies.add(new Hobby("Hydroponics", 32L));
        hobbies.add(new Hobby("Aquaponics", 32L));
        hobbies.add(new Hobby("Composting", 32L));
        hobbies.add(new Hobby("Rainwater Harvesting", 32L));
        hobbies.add(new Hobby("Eco-Building", 32L));
        hobbies.add(new Hobby("Upcycling Projects", 32L));
        hobbies.add(new Hobby("Zero-Waste Living", 32L));
        hobbies.add(new Hobby("Bamboo Crafting", 32L));

// Martial Arts subcategories (parentId = 33)
        hobbies.add(new Hobby("Kendo", 33L));
        hobbies.add(new Hobby("Aikido", 33L));
        hobbies.add(new Hobby("Muay Thai", 33L));
        hobbies.add(new Hobby("Brazilian Jiu-Jitsu", 33L));
        hobbies.add(new Hobby("Krav Maga", 33L));
        hobbies.add(new Hobby("Wing Chun", 33L));
        hobbies.add(new Hobby("Kung Fu", 33L));
        hobbies.add(new Hobby("Judo", 33L));
        hobbies.add(new Hobby("Taekwondo", 33L));
        hobbies.add(new Hobby("Hapkido", 33L));

// Collectibles subcategories (parentId = 34)
        hobbies.add(new Hobby("Vinyl Records", 34L));
        hobbies.add(new Hobby("Sports Memorabilia", 34L));
        hobbies.add(new Hobby("Movie Props", 34L));
        hobbies.add(new Hobby("Autographs", 34L));
        hobbies.add(new Hobby("Trading Cards", 34L));
        hobbies.add(new Hobby("Diecast Models", 34L));
        hobbies.add(new Hobby("Vintage Toys", 34L));
        hobbies.add(new Hobby("Art Collecting", 34L));
        hobbies.add(new Hobby("Mineral Specimens", 34L));
        hobbies.add(new Hobby("Militaria", 34L));

// Board Sports subcategories (parentId = 35)
        hobbies.add(new Hobby("Skateboarding", 35L));
        hobbies.add(new Hobby("Longboarding", 35L));
        hobbies.add(new Hobby("Snowboarding", 35L));
        hobbies.add(new Hobby("Surfing", 35L));
        hobbies.add(new Hobby("Paddleboarding", 35L));
        hobbies.add(new Hobby("Sandboarding", 35L));
        hobbies.add(new Hobby("Wakeboarding", 35L));
        hobbies.add(new Hobby("Kiteboarding", 35L));
        hobbies.add(new Hobby("Mountainboarding", 35L));
        hobbies.add(new Hobby("Streetboarding", 35L));

// Water Activities subcategories (parentId = 36)
        hobbies.add(new Hobby("Freediving", 36L));
        hobbies.add(new Hobby("Spearfishing", 36L));
        hobbies.add(new Hobby("Underwater Photography", 36L));
        hobbies.add(new Hobby("Underwater Hockey", 36L));
        hobbies.add(new Hobby("Synchronized Swimming", 36L));
        hobbies.add(new Hobby("Water Polo", 36L));
        hobbies.add(new Hobby("Dock Diving", 36L));
        hobbies.add(new Hobby("River Tubing", 36L));
        hobbies.add(new Hobby("Parasailing", 36L));
        hobbies.add(new Hobby("Flyboarding", 36L));

// Mind Sports subcategories (parentId = 37)
        hobbies.add(new Hobby("Chess", 37L));
        hobbies.add(new Hobby("Go", 37L));
        hobbies.add(new Hobby("Bridge", 37L));
        hobbies.add(new Hobby("Poker", 37L));
        hobbies.add(new Hobby("Backgammon", 37L));
        hobbies.add(new Hobby("Mahjong", 37L));
        hobbies.add(new Hobby("Scrabble", 37L));
        hobbies.add(new Hobby("Sudoku", 37L));
        hobbies.add(new Hobby("Crossword Puzzles", 37L));
        hobbies.add(new Hobby("Memory Sports", 37L));

// Fiber Arts subcategories (parentId = 38)
        hobbies.add(new Hobby("Spinning Yarn", 38L));
        hobbies.add(new Hobby("Weaving", 38L));
        hobbies.add(new Hobby("Felting", 38L));
        hobbies.add(new Hobby("Tapestry", 38L));
        hobbies.add(new Hobby("Rug Hooking", 38L));
        hobbies.add(new Hobby("Lace Making", 38L));
        hobbies.add(new Hobby("Quilting", 38L));
        hobbies.add(new Hobby("Embroidery", 38L));
        hobbies.add(new Hobby("Needlepoint", 38L));
        hobbies.add(new Hobby("Macrame", 38L));

// Home Brewing subcategories (parentId = 39)
        hobbies.add(new Hobby("Beer Brewing", 39L));
        hobbies.add(new Hobby("Mead Making", 39L));
        hobbies.add(new Hobby("Wine Making", 39L));
        hobbies.add(new Hobby("Cider Making", 39L));
        hobbies.add(new Hobby("Sake Brewing", 39L));
        hobbies.add(new Hobby("Distilling", 39L));
        hobbies.add(new Hobby("Kombucha Brewing", 39L));
        hobbies.add(new Hobby("Kefir Making", 39L));
        hobbies.add(new Hobby("Kvass Brewing", 39L));
        hobbies.add(new Hobby("Root Beer Brewing", 39L));

// Geocaching subcategories (parentId = 40)
        hobbies.add(new Hobby("Traditional Cache", 40L));
        hobbies.add(new Hobby("Multi-Cache", 40L));
        hobbies.add(new Hobby("Mystery Cache", 40L));
        hobbies.add(new Hobby("EarthCache", 40L));
        hobbies.add(new Hobby("Letterbox Hybrid", 40L));
        hobbies.add(new Hobby("Virtual Cache", 40L));
        hobbies.add(new Hobby("Wherigo Cache", 40L));
        hobbies.add(new Hobby("Event Cache", 40L));
        hobbies.add(new Hobby("Mega-Event Cache", 40L));
        hobbies.add(new Hobby("Cache In Trash Out", 40L));

// Cosplay subcategories (parentId = 41)
        hobbies.add(new Hobby("Character Design", 41L));
        hobbies.add(new Hobby("Prop Making", 41L));
        hobbies.add(new Hobby("Armor Crafting", 41L));
        hobbies.add(new Hobby("Wig Styling", 41L));
        hobbies.add(new Hobby("Makeup Effects", 41L));
        hobbies.add(new Hobby("Foam Crafting", 41L));
        hobbies.add(new Hobby("3D Printing Props", 41L));
        hobbies.add(new Hobby("Photoshoots", 41L));
        hobbies.add(new Hobby("Performance Cosplay", 41L));
        hobbies.add(new Hobby("Group Cosplay", 41L));

// Vintage Cars subcategories (parentId = 42)
        hobbies.add(new Hobby("Classic Car Restoration", 42L));
        hobbies.add(new Hobby("Hot Rod Building", 42L));
        hobbies.add(new Hobby("Rat Rod Building", 42L));
        hobbies.add(new Hobby("Vintage Racing", 42L));
        hobbies.add(new Hobby("Car Shows", 42L));
        hobbies.add(new Hobby("Automobilia Collecting", 42L));
        hobbies.add(new Hobby("Vintage Part Hunting", 42L));
        hobbies.add(new Hobby("Barn Finds", 42L));
        hobbies.add(new Hobby("Model Car Building", 42L));
        hobbies.add(new Hobby("Vintage Car Photography", 42L));

// Aquariums subcategories (parentId = 43)
        hobbies.add(new Hobby("Freshwater Tanks", 43L));
        hobbies.add(new Hobby("Saltwater Tanks", 43L));
        hobbies.add(new Hobby("Reef Keeping", 43L));
        hobbies.add(new Hobby("Planted Tanks", 43L));
        hobbies.add(new Hobby("Aquascaping", 43L));
        hobbies.add(new Hobby("Breeding Fish", 43L));
        hobbies.add(new Hobby("Shrimp Keeping", 43L));
        hobbies.add(new Hobby("Coral Propagation", 43L));
        hobbies.add(new Hobby("Aquatic Photography", 43L));
        hobbies.add(new Hobby("Paludariums", 43L));

// Bonsai subcategories (parentId = 44)
        hobbies.add(new Hobby("Indoor Bonsai", 44L));
        hobbies.add(new Hobby("Outdoor Bonsai", 44L));
        hobbies.add(new Hobby("Tropical Bonsai", 44L));
        hobbies.add(new Hobby("Conifer Bonsai", 44L));
        hobbies.add(new Hobby("Deciduous Bonsai", 44L));
        hobbies.add(new Hobby("Bonsai Wiring", 44L));
        hobbies.add(new Hobby("Bonsai Pruning", 44L));
        hobbies.add(new Hobby("Bonsai Pottery", 44L));
        hobbies.add(new Hobby("Bonsai Exhibitions", 44L));
        hobbies.add(new Hobby("Bonsai Photography", 44L));

// Amateur Radio subcategories (parentId = 45)
        hobbies.add(new Hobby("HF Operating", 45L));
        hobbies.add(new Hobby("VHF/UHF Operating", 45L));
        hobbies.add(new Hobby("DXing", 45L));
        hobbies.add(new Hobby("Contesting", 45L));
        hobbies.add(new Hobby("QSL Cards", 45L));
        hobbies.add(new Hobby("Antenna Building", 45L));
        hobbies.add(new Hobby("Satellite Communication", 45L));
        hobbies.add(new Hobby("Morse Code", 45L));
        hobbies.add(new Hobby("Fox Hunting", 45L));
        hobbies.add(new Hobby("Emergency Communication", 45L));

// Lock Picking subcategories (parentId = 46)
        hobbies.add(new Hobby("Pin Tumbler Locks", 46L));
        hobbies.add(new Hobby("Wafer Locks", 46L));
        hobbies.add(new Hobby("Disc Detainer Locks", 46L));
        hobbies.add(new Hobby("Dimple Locks", 46L));
        hobbies.add(new Hobby("Lever Locks", 46L));
        hobbies.add(new Hobby("Combination Locks", 46L));
        hobbies.add(new Hobby("Challenge Locks", 46L));
        hobbies.add(new Hobby("Lock Sport Competitions", 46L));
        hobbies.add(new Hobby("Lock Making", 46L));
        hobbies.add(new Hobby("Safe Cracking", 46L));

// Escapology subcategories (parentId = 47)
        hobbies.add(new Hobby("Handcuff Escapes", 47L));
        hobbies.add(new Hobby("Rope Escapes", 47L));
        hobbies.add(new Hobby("Straightjacket Escapes", 47L));
        hobbies.add(new Hobby("Water Tank Escapes", 47L));
        hobbies.add(new Hobby("Buried Alive Escapes", 47L));
        hobbies.add(new Hobby("Chain Escapes", 47L));
        hobbies.add(new Hobby("Box Escapes", 47L));
        hobbies.add(new Hobby("Suspension Escapes", 47L));
        hobbies.add(new Hobby("Ice Block Escapes", 47L));
        hobbies.add(new Hobby("Performance Escapology", 47L));

// Stargazing subcategories (parentId = 48)
        hobbies.add(new Hobby("Naked Eye Astronomy", 48L));
        hobbies.add(new Hobby("Binocular Astronomy", 48L));
        hobbies.add(new Hobby("Telescope Astronomy", 48L));
        hobbies.add(new Hobby("Planetary Observation", 48L));
        hobbies.add(new Hobby("Deep Sky Observation", 48L));
        hobbies.add(new Hobby("Meteor Shower Watching", 48L));
        hobbies.add(new Hobby("Satellite Tracking", 48L));
        hobbies.add(new Hobby("Aurora Watching", 48L));
        hobbies.add(new Hobby("Eclipse Chasing", 48L));

        hobbyRepository.saveAll(hobbies);
    }

}