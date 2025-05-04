package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.Hobby;
import com.talk_space.model.domain.Speciality;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.SpecialityDto;
import com.talk_space.repository.SpecialityRepository;
import com.talk_space.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialityService {


    private final SpecialityRepository specialityRepository;

    private final UserRepository userRepository;


    public Speciality save(Speciality speciality) {
        return specialityRepository.save(speciality);
    }

    public Optional<Speciality> getSpecialityById(Long id) {

        return specialityRepository.findById(id);
    }

    public List<Speciality> getAll() {
        return specialityRepository.findAll();
    }

    public Optional<Speciality> getSpecialityByName(String specialityName){
        return specialityRepository.findSpecialityByName(specialityName);
    }
    public Speciality update(Speciality speciality) {
        return specialityRepository.save(speciality);
    }

    public void deleteSpecialityById(Long id) {
        specialityRepository.deleteById(id);
    }

    public void deleteSpecialityByName(String name) {
        specialityRepository.deleteSpecialityByName(name);
    }

    public List<Speciality> saveSpecialities(List<Speciality> specialities) {
        return specialityRepository.saveAll(specialities);
    }

    public String addSpecialityForUser(SpecialityDto specialityDto) {
        if (specialityDto.getSpecialities().size() > 5) {
            throw new IllegalArgumentException("The number of specialities cannot exceed 5");
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(specialityDto.getUserName());
        if (optionalUser.isEmpty()){
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = optionalUser.get();
        user.getSpecialities().clear();


        for (int i = 0; i < specialityDto.getSpecialities().size(); i++) {
            user.getSpecialities().add(specialityRepository.findSpecialityByName((specialityDto.getSpecialities().get(i)).getName()).get());
        };
        userRepository.save(user);

        return ("Specialities added successfully.");
    }

    @PostConstruct
    public void fillDb() {
        List<Speciality> specialties = new ArrayList<>();

        // Parent specialties (with null parentId)
        specialties.add(new Speciality("Software Engineer", null));
        specialties.add(new Speciality("Doctor", null));
        specialties.add(new Speciality("Teacher", null));
        specialties.add(new Speciality("Nurse", null));
        specialties.add(new Speciality("Artist", null));
        specialties.add(new Speciality("Writer", null));
        specialties.add(new Speciality("Photographer", null));
        specialties.add(new Speciality("Musician", null));
        specialties.add(new Speciality("Chef", null));
        specialties.add(new Speciality("Architect", null));
        specialties.add(new Speciality("Lawyer", null));
        specialties.add(new Speciality("Engineer", null));
        specialties.add(new Speciality("Scientist", null));
        specialties.add(new Speciality("Sales Manager", null));
        specialties.add(new Speciality("Accountant", null));
        specialties.add(new Speciality("Electrician", null));
        specialties.add(new Speciality("Plumber", null));
        specialties.add(new Speciality("Civil Engineer", null));
        specialties.add(new Speciality("Web Developer", null));
        specialties.add(new Speciality("Graphic Designer", null));
        specialties.add(new Speciality("Marketing Manager", null));
        specialties.add(new Speciality("HR Specialist", null));
        specialties.add(new Speciality("Project Manager", null));
        specialties.add(new Speciality("Dentist", null));
        specialties.add(new Speciality("Psychologist", null));
        specialties.add(new Speciality("Veterinarian", null));
        specialties.add(new Speciality("Social Worker", null));
        specialties.add(new Speciality("Data Analyst", null));
        specialties.add(new Speciality("Entrepreneur", null));
        specialties.add(new Speciality("Real Estate Agent", null));
        specialties.add(new Speciality("Mechanic", null));
        specialties.add(new Speciality("Flight Attendant", null));
        specialties.add(new Speciality("Tour Guide", null));
        specialties.add(new Speciality("Journalist", null));
        specialties.add(new Speciality("Event Planner", null));
        specialties.add(new Speciality("Construction Worker", null));
        specialties.add(new Speciality("Librarian", null));
        specialties.add(new Speciality("Fashion Designer", null));
        specialties.add(new Speciality("Interior Designer", null));
        specialties.add(new Speciality("Public Relations Specialist", null));
        specialties.add(new Speciality("Customer Service Representative", null));
        specialties.add(new Speciality("Digital Marketer", null));
        specialties.add(new Speciality("SEO Specialist", null));
        specialties.add(new Speciality("Business Consultant", null));
        specialties.add(new Speciality("Content Creator", null));
        specialties.add(new Speciality("Translator", null));
        specialties.add(new Speciality("Fitness Trainer", null));
        specialties.add(new Speciality("Hairdresser", null));
        specialties.add(new Speciality("Barista", null));
        specialties.add(new Speciality("Baker", null));
        specialties.add(new Speciality("Security Guard", null));
        specialties.add(new Speciality("Pharmacist", null));


        // Child specialties (with specific parentIds)
        // Software Engineer subcategories
        specialties.add(new Speciality("Frontend Development", 1L));
        specialties.add(new Speciality("Backend Development", 1L));
        specialties.add(new Speciality("Full-Stack Development", 1L));
        specialties.add(new Speciality("DevOps Engineering", 1L));
        specialties.add(new Speciality("Mobile Development", 1L));
        specialties.add(new Speciality("Data Engineering", 1L));
        specialties.add(new Speciality("Machine Learning & AI Engineering", 1L));
        specialties.add(new Speciality("Cloud Engineering", 1L));
        specialties.add(new Speciality("Embedded Systems Engineering", 1L));
        specialties.add(new Speciality("Game Development", 1L));
        specialties.add(new Speciality("Security Engineering", 1L));
        specialties.add(new Speciality("Blockchain Development", 1L));
        specialties.add(new Speciality("Quality Assurance (QA) & Test Automation", 1L));
        specialties.add(new Speciality("Systems Programming", 1L));
        specialties.add(new Speciality("AR/VR Development", 1L));
        specialties.add(new Speciality("Software Architecture", 1L));
        specialties.add(new Speciality("Database Engineering", 1L));
        specialties.add(new Speciality("API Development", 1L));
        specialties.add(new Speciality("Technical Writing", 1L));
        specialties.add(new Speciality("Product Management (Technical)", 1L));

        // Doctor subcategories
        specialties.add(new Speciality("General Practitioner (GP)", 2L));
        specialties.add(new Speciality("Pediatrics", 2L));
        specialties.add(new Speciality("Cardiology", 2L));
        specialties.add(new Speciality("Oncology", 2L));
        specialties.add(new Speciality("Neurology", 2L));
        specialties.add(new Speciality("Orthopedics", 2L));
        specialties.add(new Speciality("Dermatology", 2L));
        specialties.add(new Speciality("Psychiatry", 2L));
        specialties.add(new Speciality("Radiology", 2L));
        specialties.add(new Speciality("Anesthesiology", 2L));
        specialties.add(new Speciality("Emergency Medicine", 2L));
        specialties.add(new Speciality("Obstetrics and Gynecology (OB/GYN)", 2L));
        specialties.add(new Speciality("Endocrinology", 2L));
        specialties.add(new Speciality("Gastroenterology", 2L));
        specialties.add(new Speciality("Pulmonology", 2L));
        specialties.add(new Speciality("Nephrology", 2L));
        specialties.add(new Speciality("Rheumatology", 2L));
        specialties.add(new Speciality("Infectious Disease", 2L));
        specialties.add(new Speciality("Urology", 2L));
        specialties.add(new Speciality("Ophthalmology", 2L));
        specialties.add(new Speciality("Pathology", 2L));
        specialties.add(new Speciality("Sports Medicine", 2L));
        specialties.add(new Speciality("Geriatrics", 2L));
        specialties.add(new Speciality("Allergy and Immunology", 2L));
        specialties.add(new Speciality("Hematology", 2L));
        specialties.add(new Speciality("Plastic Surgery", 2L));
        specialties.add(new Speciality("Otolaryngology (ENT)", 2L));
        specialties.add(new Speciality("Family Medicine", 2L));
        specialties.add(new Speciality("Preventive Medicine", 2L));

        // Teacher subcategories
        specialties.add(new Speciality("Elementary School Teacher", 3L));
        specialties.add(new Speciality("High School Teacher", 3L));
        specialties.add(new Speciality("College Professor", 3L));
        specialties.add(new Speciality("Special Education Teacher", 3L));
        specialties.add(new Speciality("ESL Teacher", 3L));
        specialties.add(new Speciality("Music Teacher", 3L));
        specialties.add(new Speciality("Art Teacher", 3L));
        specialties.add(new Speciality("Physical Education Teacher", 3L));
        specialties.add(new Speciality("Science Teacher", 3L));
        specialties.add(new Speciality("Mathematics Teacher", 3L));
        specialties.add(new Speciality("History Teacher", 3L));
        specialties.add(new Speciality("Language Teacher", 3L));
        specialties.add(new Speciality("Online Educator", 3L));
        specialties.add(new Speciality("Tutor", 3L));
        specialties.add(new Speciality("Educational Consultant", 3L));

        // Nurse subcategories
        specialties.add(new Speciality("Registered Nurse", 4L));
        specialties.add(new Speciality("Pediatric Nurse", 4L));
        specialties.add(new Speciality("Intensive Care Unit (ICU) Nurse", 4L));
        specialties.add(new Speciality("Emergency Room (ER) Nurse", 4L));
        specialties.add(new Speciality("Surgical Nurse", 4L));
        specialties.add(new Speciality("Nurse Practitioner", 4L));
        specialties.add(new Speciality("Geriatric Nurse", 4L));
        specialties.add(new Speciality("Oncology Nurse", 4L));
        specialties.add(new Speciality("Cardiac Nurse", 4L));
        specialties.add(new Speciality("Psychiatric Nurse", 4L));
        specialties.add(new Speciality("Travel Nurse", 4L));
        specialties.add(new Speciality("Home Health Nurse", 4L));
        specialties.add(new Speciality("Public Health Nurse", 4L));
        specialties.add(new Speciality("Nurse Educator", 4L));
        specialties.add(new Speciality("Occupational Health Nurse", 4L));

        // Artist subcategories
        specialties.add(new Speciality("Painter", 5L));
        specialties.add(new Speciality("Illustrator", 5L));
        specialties.add(new Speciality("Sculptor", 5L));
        specialties.add(new Speciality("Digital Artist", 5L));
        specialties.add(new Speciality("Tattoo Artist", 5L));
        specialties.add(new Speciality("Muralist", 5L));
        specialties.add(new Speciality("Calligrapher", 5L));
        specialties.add(new Speciality("Concept Artist", 5L));
        specialties.add(new Speciality("Caricature Artist", 5L));
        specialties.add(new Speciality("Cartoonist", 5L));

        // Writer subcategories
        specialties.add(new Speciality("Author", 6L));
        specialties.add(new Speciality("Copywriter", 6L));
        specialties.add(new Speciality("Editor", 6L));
        specialties.add(new Speciality("Screenwriter", 6L));
        specialties.add(new Speciality("Technical Writer", 6L));
        specialties.add(new Speciality("Poet", 6L));
        specialties.add(new Speciality("Content Writer", 6L));
        specialties.add(new Speciality("Ghostwriter", 6L));
        specialties.add(new Speciality("Speechwriter", 6L));

        // Photographer subcategories
        specialties.add(new Speciality("Portrait Photographer", 7L));
        specialties.add(new Speciality("Wedding Photographer", 7L));
        specialties.add(new Speciality("Wildlife Photographer", 7L));
        specialties.add(new Speciality("Fashion Photographer", 7L));
        specialties.add(new Speciality("Sports Photographer", 7L));
        specialties.add(new Speciality("Photojournalist", 7L));
        specialties.add(new Speciality("Commercial Photographer", 7L));
        specialties.add(new Speciality("Event Photographer", 7L));
        specialties.add(new Speciality("Food Photographer", 7L));
        specialties.add(new Speciality("Street Photographer", 7L));

        // Musician subcategories
        specialties.add(new Speciality("Singer", 8L));
        specialties.add(new Speciality("Composer", 8L));
        specialties.add(new Speciality("Music Producer", 8L));
        specialties.add(new Speciality("Sound Engineer", 8L));
        specialties.add(new Speciality("Guitarist", 8L));
        specialties.add(new Speciality("Violinist", 8L));
        specialties.add(new Speciality("Drummer", 8L));
        specialties.add(new Speciality("DJ", 8L));
        specialties.add(new Speciality("Pianist", 8L));
        specialties.add(new Speciality("Conductor", 8L));

        // Chef subcategories
        specialties.add(new Speciality("Pastry Chef", 9L));
        specialties.add(new Speciality("Sous Chef", 9L));
        specialties.add(new Speciality("Executive Chef", 9L));
        specialties.add(new Speciality("Private Chef", 9L));
        specialties.add(new Speciality("Catering Chef", 9L));
        specialties.add(new Speciality("Personal Chef", 9L));
        specialties.add(new Speciality("Sushi Chef", 9L));
        specialties.add(new Speciality("Grill Chef", 9L));
        specialties.add(new Speciality("Restaurant Chef", 9L));
        specialties.add(new Speciality("Banquet Chef", 9L));

        // Architect subcategories
        specialties.add(new Speciality("Residential Architect", 10L));
        specialties.add(new Speciality("Commercial Architect", 10L));
        specialties.add(new Speciality("Landscape Architect", 10L));
        specialties.add(new Speciality("Interior Architect", 10L));
        specialties.add(new Speciality("Urban Planner", 10L));
        specialties.add(new Speciality("Restoration Architect", 10L));
        specialties.add(new Speciality("Sustainable Architect", 10L));
        specialties.add(new Speciality("Industrial Architect", 10L));
        specialties.add(new Speciality("BIM Specialist", 10L));

        // Lawyer subcategories
        specialties.add(new Speciality("Corporate Lawyer", 11L));
        specialties.add(new Speciality("Criminal Lawyer", 11L));
        specialties.add(new Speciality("Family Lawyer", 11L));
        specialties.add(new Speciality("Intellectual Property Lawyer", 11L));
        specialties.add(new Speciality("Environmental Lawyer", 11L));
        specialties.add(new Speciality("Tax Lawyer", 11L));
        specialties.add(new Speciality("Employment Lawyer", 11L));
        specialties.add(new Speciality("Human Rights Lawyer", 11L));
        specialties.add(new Speciality("Real Estate Lawyer", 11L));

        // Engineer subcategories
        specialties.add(new Speciality("Mechanical Engineer", 12L));
        specialties.add(new Speciality("Electrical Engineer", 12L));
        specialties.add(new Speciality("Aerospace Engineer", 12L));
        specialties.add(new Speciality("Biomedical Engineer", 12L));
        specialties.add(new Speciality("Chemical Engineer", 12L));
        specialties.add(new Speciality("Industrial Engineer", 12L));
        specialties.add(new Speciality("Automotive Engineer", 12L));
        specialties.add(new Speciality("Petroleum Engineer", 12L));

        // Scientist subcategories
        specialties.add(new Speciality("Physicist", 13L));
        specialties.add(new Speciality("Chemist", 13L));
        specialties.add(new Speciality("Biologist", 13L));
        specialties.add(new Speciality("Astronomer", 13L));
        specialties.add(new Speciality("Geologist", 13L));
        specialties.add(new Speciality("Neuroscientist", 13L));
        specialties.add(new Speciality("Environmental Scientist", 13L));
        specialties.add(new Speciality("Data Scientist", 13L));
        specialties.add(new Speciality("Materials Scientist", 13L));
        specialties.add(new Speciality("Marine Biologist", 13L));

        // Sales Manager subcategories
        specialties.add(new Speciality("Regional Sales Manager", 14L));
        specialties.add(new Speciality("Business Development Manager", 14L));
        specialties.add(new Speciality("Retail Sales Manager", 14L));
        specialties.add(new Speciality("Inside Sales Manager", 14L));
        specialties.add(new Speciality("B2B Sales Manager", 14L));
        specialties.add(new Speciality("Account Sales Manager", 14L));
        specialties.add(new Speciality("Direct Sales Manager", 14L));
        specialties.add(new Speciality("Pharmaceutical Sales Manager", 14L));
        specialties.add(new Speciality("E-commerce Sales Manager", 14L));
        specialties.add(new Speciality("Field Sales Manager", 14L));

        // Accountant subcategories
        specialties.add(new Speciality("Financial Accountant", 15L));
        specialties.add(new Speciality("Tax Accountant", 15L));
        specialties.add(new Speciality("Forensic Accountant", 15L));
        specialties.add(new Speciality("Management Accountant", 15L));
        specialties.add(new Speciality("Cost Accountant", 15L));
        specialties.add(new Speciality("Auditor", 15L));
        specialties.add(new Speciality("Government Accountant", 15L));
        specialties.add(new Speciality("Investment Accountant", 15L));

        // Electrician subcategories
        specialties.add(new Speciality("Residential Electrician", 16L));
        specialties.add(new Speciality("Commercial Electrician", 16L));
        specialties.add(new Speciality("Industrial Electrician", 16L));
        specialties.add(new Speciality("Maintenance Electrician", 16L));
        specialties.add(new Speciality("Automotive Electrician", 16L));
        specialties.add(new Speciality("Marine Electrician", 16L));
        specialties.add(new Speciality("Lineman", 16L));
        specialties.add(new Speciality("Security Systems Electrician", 16L));
        specialties.add(new Speciality("Renewable Energy Technician", 16L));
        specialties.add(new Speciality("Electrical Inspector", 16L));

        // Plumber subcategories
        specialties.add(new Speciality("Residential Plumber", 17L));
        specialties.add(new Speciality("Commercial Plumber", 17L));
        specialties.add(new Speciality("Industrial Plumber", 17L));
        specialties.add(new Speciality("Pipefitter", 17L));
        specialties.add(new Speciality("Steamfitter", 17L));
        specialties.add(new Speciality("Sprinkler Fitter", 17L));
        specialties.add(new Speciality("Water Supply Plumber", 17L));
        specialties.add(new Speciality("Gas Plumber", 17L));
        specialties.add(new Speciality("Drainage Plumber", 17L));
        specialties.add(new Speciality("Septic System Specialist", 17L));

        // Civil Engineer subcategories
        specialties.add(new Speciality("Structural Engineer", 18L));
        specialties.add(new Speciality("Geotechnical Engineer", 18L));
        specialties.add(new Speciality("Transportation Engineer", 18L));
        specialties.add(new Speciality("Water Resources Engineer", 18L));
        specialties.add(new Speciality("Construction Engineer", 18L));
        specialties.add(new Speciality("Environmental Engineer", 18L));
        specialties.add(new Speciality("Coastal Engineer", 18L));
        specialties.add(new Speciality("Surveying Engineer", 18L));
        specialties.add(new Speciality("Urban Planning Engineer", 18L));
        specialties.add(new Speciality("Earthquake Engineer", 18L));

        // Web Developer subcategories
        specialties.add(new Speciality("Frontend Developer", 19L));
        specialties.add(new Speciality("Backend Developer", 19L));
        specialties.add(new Speciality("Full Stack Developer", 19L));
        specialties.add(new Speciality("UI/UX Developer", 19L));
        specialties.add(new Speciality("CMS Developer", 19L));
        specialties.add(new Speciality("E-commerce Developer", 19L));
        specialties.add(new Speciality("Web Security Specialist", 19L));
        specialties.add(new Speciality("WordPress Developer", 19L));
        specialties.add(new Speciality("SEO Developer", 19L));
        specialties.add(new Speciality("API Developer", 19L));

        // Graphic Designer subcategories
        specialties.add(new Speciality("Brand Identity Designer", 20L));
        specialties.add(new Speciality("Motion Graphics Designer", 20L));
        specialties.add(new Speciality("UI/UX Designer", 20L));
        specialties.add(new Speciality("Packaging Designer", 20L));
        specialties.add(new Speciality("Typography Designer", 20L));
        specialties.add(new Speciality("3D Designer", 20L));
        specialties.add(new Speciality("Advertising Designer", 20L));
        specialties.add(new Speciality("Infographic Designer", 20L));
        specialties.add(new Speciality("Print Designer", 20L));

        // Marketing Manager subcategories
        specialties.add(new Speciality("Digital Marketing Manager", 21L));
        specialties.add(new Speciality("Product Marketing Manager", 21L));
        specialties.add(new Speciality("Content Marketing Manager", 21L));
        specialties.add(new Speciality("Social Media Marketing Manager", 21L));
        specialties.add(new Speciality("SEO Manager", 21L));
        specialties.add(new Speciality("PPC Manager", 21L));
        specialties.add(new Speciality("Email Marketing Manager", 21L));
        specialties.add(new Speciality("Affiliate Marketing Manager", 21L));
        specialties.add(new Speciality("Brand Marketing Manager", 21L));
        specialties.add(new Speciality("Marketing Analyst", 21L));

        // HR Specialist subcategories
        specialties.add(new Speciality("Recruitment Specialist", 22L));
        specialties.add(new Speciality("Employee Relations Specialist", 22L));
        specialties.add(new Speciality("Compensation and Benefits Specialist", 22L));
        specialties.add(new Speciality("HR Compliance Specialist", 22L));
        specialties.add(new Speciality("Training and Development Specialist", 22L));
        specialties.add(new Speciality("HR Analyst", 22L));
        specialties.add(new Speciality("HR Generalist", 22L));
        specialties.add(new Speciality("HR Consultant", 22L));
        specialties.add(new Speciality("Diversity and Inclusion Specialist", 22L));
        specialties.add(new Speciality("Workforce Planning Specialist", 22L));

        // Project Manager subcategories
        specialties.add(new Speciality("Construction Project Manager", 23L));
        specialties.add(new Speciality("IT Project Manager", 23L));
        specialties.add(new Speciality("Agile Project Manager", 23L));
        specialties.add(new Speciality("Healthcare Project Manager", 23L));
        specialties.add(new Speciality("Marketing Project Manager", 23L));
        specialties.add(new Speciality("Finance Project Manager", 23L));
        specialties.add(new Speciality("Retail Project Manager", 23L));
        specialties.add(new Speciality("Engineering Project Manager", 23L));
        specialties.add(new Speciality("Government Project Manager", 23L));
        specialties.add(new Speciality("Education Project Manager", 23L));

        // Dentist subcategories
        specialties.add(new Speciality("General Dentist", 24L));
        specialties.add(new Speciality("Orthodontist", 24L));
        specialties.add(new Speciality("Pediatric Dentist", 24L));
        specialties.add(new Speciality("Oral Surgeon", 24L));
        specialties.add(new Speciality("Endodontist", 24L));
        specialties.add(new Speciality("Periodontist", 24L));
        specialties.add(new Speciality("Prosthodontist", 24L));
        specialties.add(new Speciality("Dental Hygienist", 24L));
        specialties.add(new Speciality("Dental Technician", 24L));
        specialties.add(new Speciality("Public Health Dentist", 24L));

        // Psychologist subcategories
        specialties.add(new Speciality("Clinical Psychologist", 25L));
        specialties.add(new Speciality("Counseling Psychologist", 25L));
        specialties.add(new Speciality("Educational Psychologist", 25L));
        specialties.add(new Speciality("Forensic Psychologist", 25L));
        specialties.add(new Speciality("Industrial-Organizational Psychologist", 25L));
        specialties.add(new Speciality("Neuropsychologist", 25L));
        specialties.add(new Speciality("Sports Psychologist", 25L));
        specialties.add(new Speciality("Health Psychologist", 25L));
        specialties.add(new Speciality("Rehabilitation Psychologist", 25L));
        specialties.add(new Speciality("Developmental Psychologist", 25L));

        // Veterinarian subcategories
        specialties.add(new Speciality("Small Animal Veterinarian", 26L));
        specialties.add(new Speciality("Large Animal Veterinarian", 26L));
        specialties.add(new Speciality("Wildlife Veterinarian", 26L));
        specialties.add(new Speciality("Equine Veterinarian", 26L));
        specialties.add(new Speciality("Veterinary Surgeon", 26L));
        specialties.add(new Speciality("Veterinary Pathologist", 26L));
        specialties.add(new Speciality("Veterinary Pharmacologist", 26L));
        specialties.add(new Speciality("Zoo Veterinarian", 26L));
        specialties.add(new Speciality("Marine Veterinarian", 26L));
        specialties.add(new Speciality("Veterinary Behaviorist", 26L));

        // Social Worker subcategories
        specialties.add(new Speciality("Child Welfare Social Worker", 27L));
        specialties.add(new Speciality("School Social Worker", 27L));
        specialties.add(new Speciality("Medical Social Worker", 27L));
        specialties.add(new Speciality("Substance Abuse Social Worker", 27L));
        specialties.add(new Speciality("Mental Health Social Worker", 27L));
        specialties.add(new Speciality("Geriatric Social Worker", 27L));
        specialties.add(new Speciality("Forensic Social Worker", 27L));
        specialties.add(new Speciality("Community Social Worker", 27L));
        specialties.add(new Speciality("Hospice Social Worker", 27L));
        specialties.add(new Speciality("Policy Social Worker", 27L));

        // Data Analyst subcategories
        specialties.add(new Speciality("Business Data Analyst", 28L));
        specialties.add(new Speciality("Financial Data Analyst", 28L));
        specialties.add(new Speciality("Marketing Data Analyst", 28L));
        specialties.add(new Speciality("Healthcare Data Analyst", 28L));
        specialties.add(new Speciality("Operations Data Analyst", 28L));
        specialties.add(new Speciality("Retail Data Analyst", 28L));
        specialties.add(new Speciality("Fraud Detection Analyst", 28L));
        specialties.add(new Speciality("Risk Analyst", 28L));
        specialties.add(new Speciality("Sports Data Analyst", 28L));
        specialties.add(new Speciality("Supply Chain Analyst", 28L));

        // Entrepreneur subcategories
        specialties.add(new Speciality("Startup Founder", 29L));
        specialties.add(new Speciality("Tech Entrepreneur", 29L));
        specialties.add(new Speciality("Social Entrepreneur", 29L));
        specialties.add(new Speciality("Retail Entrepreneur", 29L));
        specialties.add(new Speciality("E-commerce Entrepreneur", 29L));
        specialties.add(new Speciality("Real Estate Entrepreneur", 29L));
        specialties.add(new Speciality("Food Business Entrepreneur", 29L));
        specialties.add(new Speciality("Green Business Entrepreneur", 29L));
        specialties.add(new Speciality("Creative Entrepreneur", 29L));
        specialties.add(new Speciality("Lifestyle Entrepreneur", 29L));

        // Real Estate Agent subcategories
        specialties.add(new Speciality("Residential Real Estate Agent", 30L));
        specialties.add(new Speciality("Commercial Real Estate Agent", 30L));
        specialties.add(new Speciality("Luxury Real Estate Agent", 30L));
        specialties.add(new Speciality("Property Manager", 30L));
        specialties.add(new Speciality("Real Estate Broker", 30L));
        specialties.add(new Speciality("Investment Property Consultant", 30L));
        specialties.add(new Speciality("Real Estate Developer", 30L));
        specialties.add(new Speciality("Leasing Agent", 30L));
        specialties.add(new Speciality("Foreclosure Specialist", 30L));
        specialties.add(new Speciality("Real Estate Appraiser", 30L));

        // Mechanic subcategories
        specialties.add(new Speciality("Automotive Mechanic", 31L));
        specialties.add(new Speciality("Diesel Mechanic", 31L));
        specialties.add(new Speciality("Aircraft Mechanic", 31L));
        specialties.add(new Speciality("Marine Mechanic", 31L));
        specialties.add(new Speciality("Small Engine Mechanic", 31L));
        specialties.add(new Speciality("Heavy Equipment Mechanic", 31L));
        specialties.add(new Speciality("Motorcycle Mechanic", 31L));
        specialties.add(new Speciality("Industrial Machinery Mechanic", 31L));
        specialties.add(new Speciality("Brake and Transmission Specialist", 31L));
        specialties.add(new Speciality("Auto Body Technician", 31L));

        // Flight Attendant subcategories
        specialties.add(new Speciality("International Flight Attendant", 32L));
        specialties.add(new Speciality("Domestic Flight Attendant", 32L));
        specialties.add(new Speciality("Corporate Flight Attendant", 32L));
        specialties.add(new Speciality("Charter Flight Attendant", 32L));
        specialties.add(new Speciality("VIP Flight Attendant", 32L));
        specialties.add(new Speciality("Cabin Crew Trainer", 32L));
        specialties.add(new Speciality("In-flight Safety Instructor", 32L));
        specialties.add(new Speciality("Purser", 32L));
        specialties.add(new Speciality("Customer Service Flight Attendant", 32L));
        specialties.add(new Speciality("Private Jet Flight Attendant", 32L));

        // Tour Guide subcategories
        specialties.add(new Speciality("Historical Tour Guide", 33L));
        specialties.add(new Speciality("Adventure Tour Guide", 33L));
        specialties.add(new Speciality("Wildlife Tour Guide", 33L));
        specialties.add(new Speciality("Cultural Tour Guide", 33L));
        specialties.add(new Speciality("Eco-Tour Guide", 33L));
        specialties.add(new Speciality("Food and Wine Tour Guide", 33L));
        specialties.add(new Speciality("Museum Tour Guide", 33L));
        specialties.add(new Speciality("Cruise Ship Tour Guide", 33L));
        specialties.add(new Speciality("Urban Walking Tour Guide", 33L));
        specialties.add(new Speciality("Safari Tour Guide", 33L));

        // Journalist subcategories
        specialties.add(new Speciality("Investigative Journalist", 34L));
        specialties.add(new Speciality("Political Journalist", 34L));
        specialties.add(new Speciality("Sports Journalist", 34L));
        specialties.add(new Speciality("Entertainment Journalist", 34L));
        specialties.add(new Speciality("Broadcast Journalist", 34L));
        specialties.add(new Speciality("War Correspondent", 34L));
        specialties.add(new Speciality("Business Journalist", 34L));
        specialties.add(new Speciality("Science Journalist", 34L));
        specialties.add(new Speciality("Environmental Journalist", 34L));

        // Event Planner subcategories
        specialties.add(new Speciality("Corporate Event Planner", 35L));
        specialties.add(new Speciality("Wedding Planner", 35L));
        specialties.add(new Speciality("Conference Organizer", 35L));
        specialties.add(new Speciality("Exhibition Coordinator", 35L));
        specialties.add(new Speciality("Charity Event Planner", 35L));
        specialties.add(new Speciality("Festival Coordinator", 35L));
        specialties.add(new Speciality("Concert Organizer", 35L));
        specialties.add(new Speciality("Destination Event Planner", 35L));
        specialties.add(new Speciality("Social Events Planner", 35L));
        specialties.add(new Speciality("Product Launch Coordinator", 35L));

        // Construction Worker subcategories
        specialties.add(new Speciality("General Laborer", 36L));
        specialties.add(new Speciality("Carpenter", 36L));
        specialties.add(new Speciality("Mason", 36L));
        specialties.add(new Speciality("Welder", 36L));
        specialties.add(new Speciality("Heavy Equipment Operator", 36L));
        specialties.add(new Speciality("Steel Worker", 36L));
        specialties.add(new Speciality("Roofing Specialist", 36L));
        specialties.add(new Speciality("Concrete Finisher", 36L));

        // Librarian subcategories
        specialties.add(new Speciality("Reference Librarian", 37L));
        specialties.add(new Speciality("Children's Librarian", 37L));
        specialties.add(new Speciality("Academic Librarian", 37L));
        specialties.add(new Speciality("Archivist", 37L));
        specialties.add(new Speciality("Digital Services Librarian", 37L));
        specialties.add(new Speciality("Law Librarian", 37L));
        specialties.add(new Speciality("Medical Librarian", 37L));
        specialties.add(new Speciality("School Librarian", 37L));
        specialties.add(new Speciality("Special Collections Librarian", 37L));

        // Fashion Designer subcategories
        specialties.add(new Speciality("Couturier", 38L));
        specialties.add(new Speciality("Textile Designer", 38L));
        specialties.add(new Speciality("Fashion Illustrator", 38L));
        specialties.add(new Speciality("Costume Designer", 38L));
        specialties.add(new Speciality("Pattern Maker", 38L));
        specialties.add(new Speciality("Fashion Stylist", 38L));
        specialties.add(new Speciality("Apparel Merchandiser", 38L));
        specialties.add(new Speciality("Shoe Designer", 38L));
        specialties.add(new Speciality("Jewelry Designer", 38L));

        // Interior Designer subcategories
        specialties.add(new Speciality("Residential Interior Designer", 39L));
        specialties.add(new Speciality("Commercial Interior Designer", 39L));
        specialties.add(new Speciality("Sustainable Interior Designer", 39L));
        specialties.add(new Speciality("Lighting Designer", 39L));
        specialties.add(new Speciality("Set Designer", 39L));
        specialties.add(new Speciality("Retail Space Designer", 39L));
        specialties.add(new Speciality("Hospitality Interior Designer", 39L));
        specialties.add(new Speciality("Kitchen and Bath Designer", 39L));
        specialties.add(new Speciality("Exhibition Designer", 39L));
        specialties.add(new Speciality("Furniture Designer", 39L));

        // Public Relations Specialist subcategories
        specialties.add(new Speciality("Corporate Communications Specialist", 40L));
        specialties.add(new Speciality("Media Relations Specialist", 40L));
        specialties.add(new Speciality("Crisis Communication Manager", 40L));
        specialties.add(new Speciality("Public Affairs Officer", 40L));
        specialties.add(new Speciality("Brand Strategist", 40L));
        specialties.add(new Speciality("Event Publicist", 40L));
        specialties.add(new Speciality("Social Media PR Manager", 40L));
        specialties.add(new Speciality("Investor Relations Coordinator", 40L));
        specialties.add(new Speciality("Content and Media Strategist", 40L));
        specialties.add(new Speciality("Political Communications Specialist", 40L));

        // Customer Service Representative subcategories
        specialties.add(new Speciality("Call Center Representative", 41L));
        specialties.add(new Speciality("Help Desk Support", 41L));
        specialties.add(new Speciality("Technical Support Representative", 41L));
        specialties.add(new Speciality("Retail Customer Service Associate", 41L));
        specialties.add(new Speciality("Client Relations Manager", 41L));
        specialties.add(new Speciality("Guest Services Representative", 41L));
        specialties.add(new Speciality("Hospitality Customer Service Specialist", 41L));
        specialties.add(new Speciality("Customer Experience Manager", 41L));
        specialties.add(new Speciality("Live Chat Support Specialist", 41L));
        specialties.add(new Speciality("E-commerce Customer Support", 41L));

        // Digital Marketer subcategories
        specialties.add(new Speciality("Social Media Manager", 42L));
        specialties.add(new Speciality("PPC Specialist", 42L));
        specialties.add(new Speciality("Email Marketing Specialist", 42L));
        specialties.add(new Speciality("Growth Hacker", 42L));
        specialties.add(new Speciality("Content Marketing Strategist", 42L));

        // SEO Specialist subcategories
        specialties.add(new Speciality("On-Page SEO Specialist", 43L));
        specialties.add(new Speciality("Off-Page SEO Expert", 43L));
        specialties.add(new Speciality("Technical SEO Specialist", 43L));
        specialties.add(new Speciality("Local SEO Consultant", 43L));
        specialties.add(new Speciality("E-commerce SEO Manager", 43L));
        specialties.add(new Speciality("SEO Copywriter", 43L));

        // Business Consultant subcategories
        specialties.add(new Speciality("Management Consultant", 44L));
        specialties.add(new Speciality("Strategy Consultant", 44L));
        specialties.add(new Speciality("Financial Consultant", 44L));
        specialties.add(new Speciality("Marketing Consultant", 44L));
        specialties.add(new Speciality("Operations Consultant", 44L));
        specialties.add(new Speciality("IT Consultant", 44L));

        // Content Creator subcategories
        specialties.add(new Speciality("YouTuber", 45L));
        specialties.add(new Speciality("Blogger", 45L));
        specialties.add(new Speciality("Podcaster", 45L));
        specialties.add(new Speciality("Social Media Influencer", 45L));
        specialties.add(new Speciality("Videographer", 45L));
        specialties.add(new Speciality("Vlogger", 45L));

        // Translator subcategories
        specialties.add(new Speciality("Medical Translator", 46L));
        specialties.add(new Speciality("Legal Translator", 46L));
        specialties.add(new Speciality("Technical Translator", 46L));
        specialties.add(new Speciality("Literary Translator", 46L));
        specialties.add(new Speciality("Conference Interpreter", 46L));
        specialties.add(new Speciality("Localization Specialist", 46L));

        // Fitness Trainer subcategories
        specialties.add(new Speciality("Strength Coach", 47L));
        specialties.add(new Speciality("Yoga Instructor", 47L));
        specialties.add(new Speciality("Pilates Trainer", 47L));
        specialties.add(new Speciality("Athletic Trainer", 47L));
        specialties.add(new Speciality("Personal Trainer", 47L));
        specialties.add(new Speciality("Group Fitness Instructor", 47L));

        // Hairdresser subcategories
        specialties.add(new Speciality("Hairstylist", 48L));
        specialties.add(new Speciality("Barber", 48L));
        specialties.add(new Speciality("Colorist", 48L));
        specialties.add(new Speciality("Salon Manager", 48L));
        specialties.add(new Speciality("Wig Specialist", 48L));

        // Barista subcategories
        specialties.add(new Speciality("Espresso Specialist", 49L));
        specialties.add(new Speciality("Latte Artist", 49L));
        specialties.add(new Speciality("Café Manager", 49L));
        specialties.add(new Speciality("Coffee Roaster", 49L));
        specialties.add(new Speciality("Brew Master", 49L));

        // Baker subcategories
        specialties.add(new Speciality("Cake Decorator", 50L));
        specialties.add(new Speciality("Artisan Baker", 50L));
        specialties.add(new Speciality("Bread Maker", 50L));
        specialties.add(new Speciality("Confectioner", 50L));

        // Security Guard subcategories
        specialties.add(new Speciality("Night Watchman", 51L));
        specialties.add(new Speciality("Bodyguard", 51L));
        specialties.add(new Speciality("Event Security", 51L));
        specialties.add(new Speciality("Mall Security", 51L));
        specialties.add(new Speciality("Cyber Security Guard", 51L));

        specialties.add(new Speciality("Clinical Pharmacist", 52L));
        specialties.add(new Speciality("Hospital Pharmacist", 52L));
        specialties.add(new Speciality("Community Pharmacist", 52L));
        specialties.add(new Speciality("Industrial Pharmacist", 52L));
        specialties.add(new Speciality("Pharmaceutical Researcher", 52L));







        specialityRepository.saveAll(specialties);
    }


}
