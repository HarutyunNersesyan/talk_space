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

        hobbies.add(new Hobby("Software Engineer", null));
        hobbies.add(new Hobby("Doctor", null));
        hobbies.add(new Hobby("Teacher", null));
        hobbies.add(new Hobby("Nurse", null));
        hobbies.add(new Hobby("Artist", null));
        hobbies.add(new Hobby("Writer", null));
        hobbies.add(new Hobby("Photographer", null));
        hobbies.add(new Hobby("Musician", null));
        hobbies.add(new Hobby("Chef", null));
        hobbies.add(new Hobby("Architect", null));
        hobbies.add(new Hobby("Lawyer", null));
        hobbies.add(new Hobby("Engineer", null));
        hobbies.add(new Hobby("Scientist", null));
        hobbies.add(new Hobby("Sales Manager", null));
        hobbies.add(new Hobby("Accountant", null));
        hobbies.add(new Hobby("Electrician", null));
        hobbies.add(new Hobby("Plumber", null));
        hobbies.add(new Hobby("Civil Engineer", null));
        hobbies.add(new Hobby("Web Developer", null));
        hobbies.add(new Hobby("Graphic Designer", null));
        hobbies.add(new Hobby("Marketing Manager", null));
        hobbies.add(new Hobby("HR Specialist", null));
        hobbies.add(new Hobby("Project Manager", null));
        hobbies.add(new Hobby("Dentist", null));
        hobbies.add(new Hobby("Psychologist", null));
        hobbies.add(new Hobby("Veterinarian", null));
        hobbies.add(new Hobby("Social Worker", null));
        hobbies.add(new Hobby("Data Analyst", null));
        hobbies.add(new Hobby("Entrepreneur", null));
        hobbies.add(new Hobby("Real Estate Agent", null));
        hobbies.add(new Hobby("Mechanic", null));
        hobbies.add(new Hobby("Flight Attendant", null));
        hobbies.add(new Hobby("Tour Guide", null));
        hobbies.add(new Hobby("Journalist", null));
        hobbies.add(new Hobby("Event Planner", null));
        hobbies.add(new Hobby("Construction Worker", null));
        hobbies.add(new Hobby("Librarian", null));
        hobbies.add(new Hobby("Fashion Designer", null));
        hobbies.add(new Hobby("Interior Designer", null));
        hobbies.add(new Hobby("Public Relations Specialist", null));
        hobbies.add(new Hobby("Customer Service Representative", null));
        hobbies.add(new Hobby("Digital Marketer", null));
        hobbies.add(new Hobby("SEO Specialist", null));
        hobbies.add(new Hobby("Business Consultant", null));
        hobbies.add(new Hobby("Content Creator", null));
        hobbies.add(new Hobby("Translator", null));
        hobbies.add(new Hobby("Fitness Trainer", null));
        hobbies.add(new Hobby("Hairdresser", null));
        hobbies.add(new Hobby("Barista", null));
        hobbies.add(new Hobby("Baker", null));
        hobbies.add(new Hobby("Security Guard", null));
        hobbies.add(new Hobby("Pharmacist", null));

        // Parent hobbies (with null parentId)
        hobbies.add(new Hobby("Frontend Development", 1L));
        hobbies.add(new Hobby("Backend Development", 1L));
        hobbies.add(new Hobby("Full-Stack Development", 1L));
        hobbies.add(new Hobby("DevOps Engineering", 1L));
        hobbies.add(new Hobby("Mobile Development", 1L));
        hobbies.add(new Hobby("Data Engineering", 1L));
        hobbies.add(new Hobby("Machine Learning & AI Engineering", 1L));
        hobbies.add(new Hobby("Cloud Engineering", 1L));
        hobbies.add(new Hobby("Embedded Systems Engineering", 1L));
        hobbies.add(new Hobby("Game Development", 1L));
        hobbies.add(new Hobby("Security Engineering", 1L));
        hobbies.add(new Hobby("Blockchain Development", 1L));
        hobbies.add(new Hobby("Quality Assurance (QA) & Test Automation", 1L));
        hobbies.add(new Hobby("Systems Programming", 1L));
        hobbies.add(new Hobby("AR/VR Development", 1L));
        hobbies.add(new Hobby("Software Architecture", 1L));
        hobbies.add(new Hobby("Database Engineering", 1L));
        hobbies.add(new Hobby("API Development", 1L));
        hobbies.add(new Hobby("Technical Writing", 1L));
        hobbies.add(new Hobby("Product Management (Technical)", 1L));

        // Doctor subcategories
        hobbies.add(new Hobby("General Practitioner (GP)", 2L));
        hobbies.add(new Hobby("Pediatrics", 2L));
        hobbies.add(new Hobby("Cardiology", 2L));
        hobbies.add(new Hobby("Oncology", 2L));
        hobbies.add(new Hobby("Neurology", 2L));
        hobbies.add(new Hobby("Orthopedics", 2L));
        hobbies.add(new Hobby("Dermatology", 2L));
        hobbies.add(new Hobby("Psychiatry", 2L));
        hobbies.add(new Hobby("Radiology", 2L));
        hobbies.add(new Hobby("Anesthesiology", 2L));
        hobbies.add(new Hobby("Emergency Medicine", 2L));
        hobbies.add(new Hobby("Obstetrics and Gynecology (OB/GYN)", 2L));
        hobbies.add(new Hobby("Endocrinology", 2L));
        hobbies.add(new Hobby("Gastroenterology", 2L));
        hobbies.add(new Hobby("Pulmonology", 2L));
        hobbies.add(new Hobby("Nephrology", 2L));
        hobbies.add(new Hobby("Rheumatology", 2L));
        hobbies.add(new Hobby("Infectious Disease", 2L));
        hobbies.add(new Hobby("Urology", 2L));
        hobbies.add(new Hobby("Ophthalmology", 2L));
        hobbies.add(new Hobby("Pathology", 2L));
        hobbies.add(new Hobby("Sports Medicine", 2L));
        hobbies.add(new Hobby("Geriatrics", 2L));
        hobbies.add(new Hobby("Allergy and Immunology", 2L));
        hobbies.add(new Hobby("Hematology", 2L));
        hobbies.add(new Hobby("Plastic Surgery", 2L));
        hobbies.add(new Hobby("Otolaryngology (ENT)", 2L));
        hobbies.add(new Hobby("Family Medicine", 2L));
        hobbies.add(new Hobby("Preventive Medicine", 2L));

        // Teacher subcategories
        hobbies.add(new Hobby("Elementary School Teacher", 3L));
        hobbies.add(new Hobby("High School Teacher", 3L));
        hobbies.add(new Hobby("College Professor", 3L));
        hobbies.add(new Hobby("Special Education Teacher", 3L));
        hobbies.add(new Hobby("ESL Teacher", 3L));
        hobbies.add(new Hobby("Music Teacher", 3L));
        hobbies.add(new Hobby("Art Teacher", 3L));
        hobbies.add(new Hobby("Physical Education Teacher", 3L));
        hobbies.add(new Hobby("Science Teacher", 3L));
        hobbies.add(new Hobby("Mathematics Teacher", 3L));
        hobbies.add(new Hobby("History Teacher", 3L));
        hobbies.add(new Hobby("Language Teacher", 3L));
        hobbies.add(new Hobby("Online Educator", 3L));
        hobbies.add(new Hobby("Tutor", 3L));
        hobbies.add(new Hobby("Educational Consultant", 3L));

        // Nurse subcategories
        hobbies.add(new Hobby("Registered Nurse", 4L));
        hobbies.add(new Hobby("Pediatric Nurse", 4L));
        hobbies.add(new Hobby("Intensive Care Unit (ICU) Nurse", 4L));
        hobbies.add(new Hobby("Emergency Room (ER) Nurse", 4L));
        hobbies.add(new Hobby("Surgical Nurse", 4L));
        hobbies.add(new Hobby("Nurse Practitioner", 4L));
        hobbies.add(new Hobby("Geriatric Nurse", 4L));
        hobbies.add(new Hobby("Oncology Nurse", 4L));
        hobbies.add(new Hobby("Cardiac Nurse", 4L));
        hobbies.add(new Hobby("Psychiatric Nurse", 4L));
        hobbies.add(new Hobby("Travel Nurse", 4L));
        hobbies.add(new Hobby("Home Health Nurse", 4L));
        hobbies.add(new Hobby("Public Health Nurse", 4L));
        hobbies.add(new Hobby("Nurse Educator", 4L));
        hobbies.add(new Hobby("Occupational Health Nurse", 4L));

        // Artist subcategories
        hobbies.add(new Hobby("Painter", 5L));
        hobbies.add(new Hobby("Illustrator", 5L));
        hobbies.add(new Hobby("Sculptor", 5L));
        hobbies.add(new Hobby("Digital Artist", 5L));
        hobbies.add(new Hobby("Tattoo Artist", 5L));
        hobbies.add(new Hobby("Muralist", 5L));
        hobbies.add(new Hobby("Calligrapher", 5L));
        hobbies.add(new Hobby("Concept Artist", 5L));
        hobbies.add(new Hobby("Caricature Artist", 5L));
        hobbies.add(new Hobby("Cartoonist", 5L));

        // Writer subcategories
        hobbies.add(new Hobby("Author", 6L));
        hobbies.add(new Hobby("Copywriter", 6L));
        hobbies.add(new Hobby("Editor", 6L));
        hobbies.add(new Hobby("Screenwriter", 6L));
        hobbies.add(new Hobby("Technical Writer", 6L));
        hobbies.add(new Hobby("Poet", 6L));
        hobbies.add(new Hobby("Content Writer", 6L));
        hobbies.add(new Hobby("Ghostwriter", 6L));
        hobbies.add(new Hobby("Speechwriter", 6L));

        // Photographer subcategories
        hobbies.add(new Hobby("Portrait Photographer", 7L));
        hobbies.add(new Hobby("Wedding Photographer", 7L));
        hobbies.add(new Hobby("Wildlife Photographer", 7L));
        hobbies.add(new Hobby("Fashion Photographer", 7L));
        hobbies.add(new Hobby("Sports Photographer", 7L));
        hobbies.add(new Hobby("Photojournalist", 7L));
        hobbies.add(new Hobby("Commercial Photographer", 7L));
        hobbies.add(new Hobby("Event Photographer", 7L));
        hobbies.add(new Hobby("Food Photographer", 7L));
        hobbies.add(new Hobby("Street Photographer", 7L));

        // Musician subcategories
        hobbies.add(new Hobby("Singer", 8L));
        hobbies.add(new Hobby("Composer", 8L));
        hobbies.add(new Hobby("Music Producer", 8L));
        hobbies.add(new Hobby("Sound Engineer", 8L));
        hobbies.add(new Hobby("Guitarist", 8L));
        hobbies.add(new Hobby("Violinist", 8L));
        hobbies.add(new Hobby("Drummer", 8L));
        hobbies.add(new Hobby("DJ", 8L));
        hobbies.add(new Hobby("Pianist", 8L));
        hobbies.add(new Hobby("Conductor", 8L));

        // Chef subcategories
        hobbies.add(new Hobby("Pastry Chef", 9L));
        hobbies.add(new Hobby("Sous Chef", 9L));
        hobbies.add(new Hobby("Executive Chef", 9L));
        hobbies.add(new Hobby("Private Chef", 9L));
        hobbies.add(new Hobby("Catering Chef", 9L));
        hobbies.add(new Hobby("Personal Chef", 9L));
        hobbies.add(new Hobby("Sushi Chef", 9L));
        hobbies.add(new Hobby("Grill Chef", 9L));
        hobbies.add(new Hobby("Restaurant Chef", 9L));
        hobbies.add(new Hobby("Banquet Chef", 9L));

        // Architect subcategories
        hobbies.add(new Hobby("Residential Architect", 10L));
        hobbies.add(new Hobby("Commercial Architect", 10L));
        hobbies.add(new Hobby("Landscape Architect", 10L));
        hobbies.add(new Hobby("Interior Architect", 10L));
        hobbies.add(new Hobby("Urban Planner", 10L));
        hobbies.add(new Hobby("Restoration Architect", 10L));
        hobbies.add(new Hobby("Sustainable Architect", 10L));
        hobbies.add(new Hobby("Industrial Architect", 10L));
        hobbies.add(new Hobby("BIM Specialist", 10L));

        // Lawyer subcategories
        hobbies.add(new Hobby("Corporate Lawyer", 11L));
        hobbies.add(new Hobby("Criminal Lawyer", 11L));
        hobbies.add(new Hobby("Family Lawyer", 11L));
        hobbies.add(new Hobby("Intellectual Property Lawyer", 11L));
        hobbies.add(new Hobby("Environmental Lawyer", 11L));
        hobbies.add(new Hobby("Tax Lawyer", 11L));
        hobbies.add(new Hobby("Employment Lawyer", 11L));
        hobbies.add(new Hobby("Human Rights Lawyer", 11L));
        hobbies.add(new Hobby("Real Estate Lawyer", 11L));

        // Engineer subcategories
        hobbies.add(new Hobby("Mechanical Engineer", 12L));
        hobbies.add(new Hobby("Electrical Engineer", 12L));
        hobbies.add(new Hobby("Aerospace Engineer", 12L));
        hobbies.add(new Hobby("Biomedical Engineer", 12L));
        hobbies.add(new Hobby("Chemical Engineer", 12L));
        hobbies.add(new Hobby("Industrial Engineer", 12L));
        hobbies.add(new Hobby("Automotive Engineer", 12L));
        hobbies.add(new Hobby("Petroleum Engineer", 12L));

        // Scientist subcategories
        hobbies.add(new Hobby("Physicist", 13L));
        hobbies.add(new Hobby("Chemist", 13L));
        hobbies.add(new Hobby("Biologist", 13L));
        hobbies.add(new Hobby("Astronomer", 13L));
        hobbies.add(new Hobby("Geologist", 13L));
        hobbies.add(new Hobby("Neuroscientist", 13L));
        hobbies.add(new Hobby("Environmental Scientist", 13L));
        hobbies.add(new Hobby("Data Scientist", 13L));
        hobbies.add(new Hobby("Materials Scientist", 13L));
        hobbies.add(new Hobby("Marine Biologist", 13L));

        // Sales Manager subcategories
        hobbies.add(new Hobby("Regional Sales Manager", 14L));
        hobbies.add(new Hobby("Business Development Manager", 14L));
        hobbies.add(new Hobby("Retail Sales Manager", 14L));
        hobbies.add(new Hobby("Inside Sales Manager", 14L));
        hobbies.add(new Hobby("B2B Sales Manager", 14L));
        hobbies.add(new Hobby("Account Sales Manager", 14L));
        hobbies.add(new Hobby("Direct Sales Manager", 14L));
        hobbies.add(new Hobby("Pharmaceutical Sales Manager", 14L));
        hobbies.add(new Hobby("E-commerce Sales Manager", 14L));
        hobbies.add(new Hobby("Field Sales Manager", 14L));

        // Accountant subcategories
        hobbies.add(new Hobby("Financial Accountant", 15L));
        hobbies.add(new Hobby("Tax Accountant", 15L));
        hobbies.add(new Hobby("Forensic Accountant", 15L));
        hobbies.add(new Hobby("Management Accountant", 15L));
        hobbies.add(new Hobby("Cost Accountant", 15L));
        hobbies.add(new Hobby("Auditor", 15L));
        hobbies.add(new Hobby("Government Accountant", 15L));
        hobbies.add(new Hobby("Investment Accountant", 15L));

        // Electrician subcategories
        hobbies.add(new Hobby("Residential Electrician", 16L));
        hobbies.add(new Hobby("Commercial Electrician", 16L));
        hobbies.add(new Hobby("Industrial Electrician", 16L));
        hobbies.add(new Hobby("Maintenance Electrician", 16L));
        hobbies.add(new Hobby("Automotive Electrician", 16L));
        hobbies.add(new Hobby("Marine Electrician", 16L));
        hobbies.add(new Hobby("Lineman", 16L));
        hobbies.add(new Hobby("Security Systems Electrician", 16L));
        hobbies.add(new Hobby("Renewable Energy Technician", 16L));
        hobbies.add(new Hobby("Electrical Inspector", 16L));

        // Plumber subcategories
        hobbies.add(new Hobby("Residential Plumber", 17L));
        hobbies.add(new Hobby("Commercial Plumber", 17L));
        hobbies.add(new Hobby("Industrial Plumber", 17L));
        hobbies.add(new Hobby("Pipefitter", 17L));
        hobbies.add(new Hobby("Steamfitter", 17L));
        hobbies.add(new Hobby("Sprinkler Fitter", 17L));
        hobbies.add(new Hobby("Water Supply Plumber", 17L));
        hobbies.add(new Hobby("Gas Plumber", 17L));
        hobbies.add(new Hobby("Drainage Plumber", 17L));
        hobbies.add(new Hobby("Septic System Specialist", 17L));

        // Civil Engineer subcategories
        hobbies.add(new Hobby("Structural Engineer", 18L));
        hobbies.add(new Hobby("Geotechnical Engineer", 18L));
        hobbies.add(new Hobby("Transportation Engineer", 18L));
        hobbies.add(new Hobby("Water Resources Engineer", 18L));
        hobbies.add(new Hobby("Construction Engineer", 18L));
        hobbies.add(new Hobby("Environmental Engineer", 18L));
        hobbies.add(new Hobby("Coastal Engineer", 18L));
        hobbies.add(new Hobby("Surveying Engineer", 18L));
        hobbies.add(new Hobby("Urban Planning Engineer", 18L));
        hobbies.add(new Hobby("Earthquake Engineer", 18L));

        // Web Developer subcategories
        hobbies.add(new Hobby("Frontend Developer", 19L));
        hobbies.add(new Hobby("Backend Developer", 19L));
        hobbies.add(new Hobby("Full Stack Developer", 19L));
        hobbies.add(new Hobby("UI/UX Developer", 19L));
        hobbies.add(new Hobby("CMS Developer", 19L));
        hobbies.add(new Hobby("E-commerce Developer", 19L));
        hobbies.add(new Hobby("Web Security Specialist", 19L));
        hobbies.add(new Hobby("WordPress Developer", 19L));
        hobbies.add(new Hobby("SEO Developer", 19L));
        hobbies.add(new Hobby("API Developer", 19L));

        // Graphic Designer subcategories
        hobbies.add(new Hobby("Brand Identity Designer", 20L));
        hobbies.add(new Hobby("Motion Graphics Designer", 20L));
        hobbies.add(new Hobby("UI/UX Designer", 20L));
        hobbies.add(new Hobby("Packaging Designer", 20L));
        hobbies.add(new Hobby("Typography Designer", 20L));
        hobbies.add(new Hobby("3D Designer", 20L));
        hobbies.add(new Hobby("Advertising Designer", 20L));
        hobbies.add(new Hobby("Infographic Designer", 20L));
        hobbies.add(new Hobby("Print Designer", 20L));

        // Marketing Manager subcategories
        hobbies.add(new Hobby("Digital Marketing Manager", 21L));
        hobbies.add(new Hobby("Product Marketing Manager", 21L));
        hobbies.add(new Hobby("Content Marketing Manager", 21L));
        hobbies.add(new Hobby("Social Media Marketing Manager", 21L));
        hobbies.add(new Hobby("SEO Manager", 21L));
        hobbies.add(new Hobby("PPC Manager", 21L));
        hobbies.add(new Hobby("Email Marketing Manager", 21L));
        hobbies.add(new Hobby("Affiliate Marketing Manager", 21L));
        hobbies.add(new Hobby("Brand Marketing Manager", 21L));
        hobbies.add(new Hobby("Marketing Analyst", 21L));

        // HR Specialist subcategories
        hobbies.add(new Hobby("Recruitment Specialist", 22L));
        hobbies.add(new Hobby("Employee Relations Specialist", 22L));
        hobbies.add(new Hobby("Compensation and Benefits Specialist", 22L));
        hobbies.add(new Hobby("HR Compliance Specialist", 22L));
        hobbies.add(new Hobby("Training and Development Specialist", 22L));
        hobbies.add(new Hobby("HR Analyst", 22L));
        hobbies.add(new Hobby("HR Generalist", 22L));
        hobbies.add(new Hobby("HR Consultant", 22L));
        hobbies.add(new Hobby("Diversity and Inclusion Specialist", 22L));
        hobbies.add(new Hobby("Workforce Planning Specialist", 22L));

        // Project Manager subcategories
        hobbies.add(new Hobby("Construction Project Manager", 23L));
        hobbies.add(new Hobby("IT Project Manager", 23L));
        hobbies.add(new Hobby("Agile Project Manager", 23L));
        hobbies.add(new Hobby("Healthcare Project Manager", 23L));
        hobbies.add(new Hobby("Marketing Project Manager", 23L));
        hobbies.add(new Hobby("Finance Project Manager", 23L));
        hobbies.add(new Hobby("Retail Project Manager", 23L));
        hobbies.add(new Hobby("Engineering Project Manager", 23L));
        hobbies.add(new Hobby("Government Project Manager", 23L));
        hobbies.add(new Hobby("Education Project Manager", 23L));

        // Dentist subcategories
        hobbies.add(new Hobby("General Dentist", 24L));
        hobbies.add(new Hobby("Orthodontist", 24L));
        hobbies.add(new Hobby("Pediatric Dentist", 24L));
        hobbies.add(new Hobby("Oral Surgeon", 24L));
        hobbies.add(new Hobby("Endodontist", 24L));
        hobbies.add(new Hobby("Periodontist", 24L));
        hobbies.add(new Hobby("Prosthodontist", 24L));
        hobbies.add(new Hobby("Dental Hygienist", 24L));
        hobbies.add(new Hobby("Dental Technician", 24L));
        hobbies.add(new Hobby("Public Health Dentist", 24L));

        // Psychologist subcategories
        hobbies.add(new Hobby("Clinical Psychologist", 25L));
        hobbies.add(new Hobby("Counseling Psychologist", 25L));
        hobbies.add(new Hobby("Educational Psychologist", 25L));
        hobbies.add(new Hobby("Forensic Psychologist", 25L));
        hobbies.add(new Hobby("Industrial-Organizational Psychologist", 25L));
        hobbies.add(new Hobby("Neuropsychologist", 25L));
        hobbies.add(new Hobby("Sports Psychologist", 25L));
        hobbies.add(new Hobby("Health Psychologist", 25L));
        hobbies.add(new Hobby("Rehabilitation Psychologist", 25L));
        hobbies.add(new Hobby("Developmental Psychologist", 25L));

        // Veterinarian subcategories
        hobbies.add(new Hobby("Small Animal Veterinarian", 26L));
        hobbies.add(new Hobby("Large Animal Veterinarian", 26L));
        hobbies.add(new Hobby("Wildlife Veterinarian", 26L));
        hobbies.add(new Hobby("Equine Veterinarian", 26L));
        hobbies.add(new Hobby("Veterinary Surgeon", 26L));
        hobbies.add(new Hobby("Veterinary Pathologist", 26L));
        hobbies.add(new Hobby("Veterinary Pharmacologist", 26L));
        hobbies.add(new Hobby("Zoo Veterinarian", 26L));
        hobbies.add(new Hobby("Marine Veterinarian", 26L));
        hobbies.add(new Hobby("Veterinary Behaviorist", 26L));

        // Social Worker subcategories
        hobbies.add(new Hobby("Child Welfare Social Worker", 27L));
        hobbies.add(new Hobby("School Social Worker", 27L));
        hobbies.add(new Hobby("Medical Social Worker", 27L));
        hobbies.add(new Hobby("Substance Abuse Social Worker", 27L));
        hobbies.add(new Hobby("Mental Health Social Worker", 27L));
        hobbies.add(new Hobby("Geriatric Social Worker", 27L));
        hobbies.add(new Hobby("Forensic Social Worker", 27L));
        hobbies.add(new Hobby("Community Social Worker", 27L));
        hobbies.add(new Hobby("Hospice Social Worker", 27L));
        hobbies.add(new Hobby("Policy Social Worker", 27L));

        // Data Analyst subcategories
        hobbies.add(new Hobby("Business Data Analyst", 28L));
        hobbies.add(new Hobby("Financial Data Analyst", 28L));
        hobbies.add(new Hobby("Marketing Data Analyst", 28L));
        hobbies.add(new Hobby("Healthcare Data Analyst", 28L));
        hobbies.add(new Hobby("Operations Data Analyst", 28L));
        hobbies.add(new Hobby("Retail Data Analyst", 28L));
        hobbies.add(new Hobby("Fraud Detection Analyst", 28L));
        hobbies.add(new Hobby("Risk Analyst", 28L));
        hobbies.add(new Hobby("Sports Data Analyst", 28L));
        hobbies.add(new Hobby("Supply Chain Analyst", 28L));

        // Entrepreneur subcategories
        hobbies.add(new Hobby("Startup Founder", 29L));
        hobbies.add(new Hobby("Tech Entrepreneur", 29L));
        hobbies.add(new Hobby("Social Entrepreneur", 29L));
        hobbies.add(new Hobby("Retail Entrepreneur", 29L));
        hobbies.add(new Hobby("E-commerce Entrepreneur", 29L));
        hobbies.add(new Hobby("Real Estate Entrepreneur", 29L));
        hobbies.add(new Hobby("Food Business Entrepreneur", 29L));
        hobbies.add(new Hobby("Green Business Entrepreneur", 29L));
        hobbies.add(new Hobby("Creative Entrepreneur", 29L));
        hobbies.add(new Hobby("Lifestyle Entrepreneur", 29L));

        // Real Estate Agent subcategories
        hobbies.add(new Hobby("Residential Real Estate Agent", 30L));
        hobbies.add(new Hobby("Commercial Real Estate Agent", 30L));
        hobbies.add(new Hobby("Luxury Real Estate Agent", 30L));
        hobbies.add(new Hobby("Property Manager", 30L));
        hobbies.add(new Hobby("Real Estate Broker", 30L));
        hobbies.add(new Hobby("Investment Property Consultant", 30L));
        hobbies.add(new Hobby("Real Estate Developer", 30L));
        hobbies.add(new Hobby("Leasing Agent", 30L));
        hobbies.add(new Hobby("Foreclosure Specialist", 30L));
        hobbies.add(new Hobby("Real Estate Appraiser", 30L));

        // Mechanic subcategories
        hobbies.add(new Hobby("Automotive Mechanic", 31L));
        hobbies.add(new Hobby("Diesel Mechanic", 31L));
        hobbies.add(new Hobby("Aircraft Mechanic", 31L));
        hobbies.add(new Hobby("Marine Mechanic", 31L));
        hobbies.add(new Hobby("Small Engine Mechanic", 31L));
        hobbies.add(new Hobby("Heavy Equipment Mechanic", 31L));
        hobbies.add(new Hobby("Motorcycle Mechanic", 31L));
        hobbies.add(new Hobby("Industrial Machinery Mechanic", 31L));
        hobbies.add(new Hobby("Brake and Transmission Specialist", 31L));
        hobbies.add(new Hobby("Auto Body Technician", 31L));

        // Flight Attendant subcategories
        hobbies.add(new Hobby("International Flight Attendant", 32L));
        hobbies.add(new Hobby("Domestic Flight Attendant", 32L));
        hobbies.add(new Hobby("Corporate Flight Attendant", 32L));
        hobbies.add(new Hobby("Charter Flight Attendant", 32L));
        hobbies.add(new Hobby("VIP Flight Attendant", 32L));
        hobbies.add(new Hobby("Cabin Crew Trainer", 32L));
        hobbies.add(new Hobby("In-flight Safety Instructor", 32L));
        hobbies.add(new Hobby("Purser", 32L));
        hobbies.add(new Hobby("Customer Service Flight Attendant", 32L));
        hobbies.add(new Hobby("Private Jet Flight Attendant", 32L));

        // Tour Guide subcategories
        hobbies.add(new Hobby("Historical Tour Guide", 33L));
        hobbies.add(new Hobby("Adventure Tour Guide", 33L));
        hobbies.add(new Hobby("Wildlife Tour Guide", 33L));
        hobbies.add(new Hobby("Cultural Tour Guide", 33L));
        hobbies.add(new Hobby("Eco-Tour Guide", 33L));
        hobbies.add(new Hobby("Food and Wine Tour Guide", 33L));
        hobbies.add(new Hobby("Museum Tour Guide", 33L));
        hobbies.add(new Hobby("Cruise Ship Tour Guide", 33L));
        hobbies.add(new Hobby("Urban Walking Tour Guide", 33L));
        hobbies.add(new Hobby("Safari Tour Guide", 33L));

        // Journalist subcategories
        hobbies.add(new Hobby("Investigative Journalist", 34L));
        hobbies.add(new Hobby("Political Journalist", 34L));
        hobbies.add(new Hobby("Sports Journalist", 34L));
        hobbies.add(new Hobby("Entertainment Journalist", 34L));
        hobbies.add(new Hobby("Broadcast Journalist", 34L));
        hobbies.add(new Hobby("War Correspondent", 34L));
        hobbies.add(new Hobby("Business Journalist", 34L));
        hobbies.add(new Hobby("Science Journalist", 34L));
        hobbies.add(new Hobby("Environmental Journalist", 34L));

        // Event Planner subcategories
        hobbies.add(new Hobby("Corporate Event Planner", 35L));
        hobbies.add(new Hobby("Wedding Planner", 35L));
        hobbies.add(new Hobby("Conference Organizer", 35L));
        hobbies.add(new Hobby("Exhibition Coordinator", 35L));
        hobbies.add(new Hobby("Charity Event Planner", 35L));
        hobbies.add(new Hobby("Festival Coordinator", 35L));
        hobbies.add(new Hobby("Concert Organizer", 35L));
        hobbies.add(new Hobby("Destination Event Planner", 35L));
        hobbies.add(new Hobby("Social Events Planner", 35L));
        hobbies.add(new Hobby("Product Launch Coordinator", 35L));

        // Construction Worker subcategories
        hobbies.add(new Hobby("General Laborer", 36L));
        hobbies.add(new Hobby("Carpenter", 36L));
        hobbies.add(new Hobby("Mason", 36L));
        hobbies.add(new Hobby("Welder", 36L));
        hobbies.add(new Hobby("Heavy Equipment Operator", 36L));
        hobbies.add(new Hobby("Steel Worker", 36L));
        hobbies.add(new Hobby("Roofing Specialist", 36L));
        hobbies.add(new Hobby("Concrete Finisher", 36L));

        // Librarian subcategories
        hobbies.add(new Hobby("Reference Librarian", 37L));
        hobbies.add(new Hobby("Children's Librarian", 37L));
        hobbies.add(new Hobby("Academic Librarian", 37L));
        hobbies.add(new Hobby("Archivist", 37L));
        hobbies.add(new Hobby("Digital Services Librarian", 37L));
        hobbies.add(new Hobby("Law Librarian", 37L));
        hobbies.add(new Hobby("Medical Librarian", 37L));
        hobbies.add(new Hobby("School Librarian", 37L));
        hobbies.add(new Hobby("Special Collections Librarian", 37L));

        // Fashion Designer subcategories
        hobbies.add(new Hobby("Couturier", 38L));
        hobbies.add(new Hobby("Textile Designer", 38L));
        hobbies.add(new Hobby("Fashion Illustrator", 38L));
        hobbies.add(new Hobby("Costume Designer", 38L));
        hobbies.add(new Hobby("Pattern Maker", 38L));
        hobbies.add(new Hobby("Fashion Stylist", 38L));
        hobbies.add(new Hobby("Apparel Merchandiser", 38L));
        hobbies.add(new Hobby("Shoe Designer", 38L));
        hobbies.add(new Hobby("Jewelry Designer", 38L));

        // Interior Designer subcategories
        hobbies.add(new Hobby("Residential Interior Designer", 39L));
        hobbies.add(new Hobby("Commercial Interior Designer", 39L));
        hobbies.add(new Hobby("Sustainable Interior Designer", 39L));
        hobbies.add(new Hobby("Lighting Designer", 39L));
        hobbies.add(new Hobby("Set Designer", 39L));
        hobbies.add(new Hobby("Retail Space Designer", 39L));
        hobbies.add(new Hobby("Hospitality Interior Designer", 39L));
        hobbies.add(new Hobby("Kitchen and Bath Designer", 39L));
        hobbies.add(new Hobby("Exhibition Designer", 39L));
        hobbies.add(new Hobby("Furniture Designer", 39L));

        // Public Relations Specialist subcategories
        hobbies.add(new Hobby("Corporate Communications Specialist", 40L));
        hobbies.add(new Hobby("Media Relations Specialist", 40L));
        hobbies.add(new Hobby("Crisis Communication Manager", 40L));
        hobbies.add(new Hobby("Public Affairs Officer", 40L));
        hobbies.add(new Hobby("Brand Strategist", 40L));
        hobbies.add(new Hobby("Event Publicist", 40L));
        hobbies.add(new Hobby("Social Media PR Manager", 40L));
        hobbies.add(new Hobby("Investor Relations Coordinator", 40L));
        hobbies.add(new Hobby("Content and Media Strategist", 40L));
        hobbies.add(new Hobby("Political Communications Specialist", 40L));

        // Customer Service Representative subcategories
        hobbies.add(new Hobby("Call Center Representative", 41L));
        hobbies.add(new Hobby("Help Desk Support", 41L));
        hobbies.add(new Hobby("Technical Support Representative", 41L));
        hobbies.add(new Hobby("Retail Customer Service Associate", 41L));
        hobbies.add(new Hobby("Client Relations Manager", 41L));
        hobbies.add(new Hobby("Guest Services Representative", 41L));
        hobbies.add(new Hobby("Hospitality Customer Service Specialist", 41L));
        hobbies.add(new Hobby("Customer Experience Manager", 41L));
        hobbies.add(new Hobby("Live Chat Support Specialist", 41L));
        hobbies.add(new Hobby("E-commerce Customer Support", 41L));

        // Digital Marketer subcategories
        hobbies.add(new Hobby("Social Media Manager", 42L));
        hobbies.add(new Hobby("PPC Specialist", 42L));
        hobbies.add(new Hobby("Email Marketing Specialist", 42L));
        hobbies.add(new Hobby("Growth Hacker", 42L));
        hobbies.add(new Hobby("Content Marketing Strategist", 42L));

        // SEO Specialist subcategories
        hobbies.add(new Hobby("On-Page SEO Specialist", 43L));
        hobbies.add(new Hobby("Off-Page SEO Expert", 43L));
        hobbies.add(new Hobby("Technical SEO Specialist", 43L));
        hobbies.add(new Hobby("Local SEO Consultant", 43L));
        hobbies.add(new Hobby("E-commerce SEO Manager", 43L));
        hobbies.add(new Hobby("SEO Copywriter", 43L));

        // Business Consultant subcategories
        hobbies.add(new Hobby("Management Consultant", 44L));
        hobbies.add(new Hobby("Strategy Consultant", 44L));
        hobbies.add(new Hobby("Financial Consultant", 44L));
        hobbies.add(new Hobby("Marketing Consultant", 44L));
        hobbies.add(new Hobby("Operations Consultant", 44L));
        hobbies.add(new Hobby("IT Consultant", 44L));

        // Content Creator subcategories
        hobbies.add(new Hobby("YouTuber", 45L));
        hobbies.add(new Hobby("Blogger", 45L));
        hobbies.add(new Hobby("Podcaster", 45L));
        hobbies.add(new Hobby("Social Media Influencer", 45L));
        hobbies.add(new Hobby("Videographer", 45L));
        hobbies.add(new Hobby("Vlogger", 45L));

        // Translator subcategories
        hobbies.add(new Hobby("Medical Translator", 46L));
        hobbies.add(new Hobby("Legal Translator", 46L));
        hobbies.add(new Hobby("Technical Translator", 46L));
        hobbies.add(new Hobby("Literary Translator", 46L));
        hobbies.add(new Hobby("Conference Interpreter", 46L));
        hobbies.add(new Hobby("Localization Specialist", 46L));

        // Fitness Trainer subcategories
        hobbies.add(new Hobby("Strength Coach", 47L));
        hobbies.add(new Hobby("Yoga Instructor", 47L));
        hobbies.add(new Hobby("Pilates Trainer", 47L));
        hobbies.add(new Hobby("Athletic Trainer", 47L));
        hobbies.add(new Hobby("Personal Trainer", 47L));
        hobbies.add(new Hobby("Group Fitness Instructor", 47L));

        // Hairdresser subcategories
        hobbies.add(new Hobby("Hairstylist", 48L));
        hobbies.add(new Hobby("Barber", 48L));
        hobbies.add(new Hobby("Colorist", 48L));
        hobbies.add(new Hobby("Salon Manager", 48L));
        hobbies.add(new Hobby("Wig Specialist", 48L));

        // Barista subcategories
        hobbies.add(new Hobby("Espresso Specialist", 49L));
        hobbies.add(new Hobby("Latte Artist", 49L));
        hobbies.add(new Hobby("Café Manager", 49L));
        hobbies.add(new Hobby("Coffee Roaster", 49L));
        hobbies.add(new Hobby("Brew Master", 49L));

        // Baker subcategories
        hobbies.add(new Hobby("Cake Decorator", 50L));
        hobbies.add(new Hobby("Artisan Baker", 50L));
        hobbies.add(new Hobby("Bread Maker", 50L));
        hobbies.add(new Hobby("Confectioner", 50L));

        // Security Guard subcategories
        hobbies.add(new Hobby("Night Watchman", 51L));
        hobbies.add(new Hobby("Bodyguard", 51L));
        hobbies.add(new Hobby("Event Security", 51L));
        hobbies.add(new Hobby("Mall Security", 51L));
        hobbies.add(new Hobby("Cyber Security Guard", 51L));

        // Pharmacist subcategories
        hobbies.add(new Hobby("Clinical Pharmacist", 52L));
        hobbies.add(new Hobby("Hospital Pharmacist", 52L));
        hobbies.add(new Hobby("Community Pharmacist", 52L));
        hobbies.add(new Hobby("Industrial Pharmacist", 52L));
        hobbies.add(new Hobby("Pharmaceutical Researcher", 52L));

        hobbyRepository.saveAll(hobbies);
    }

}