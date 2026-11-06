package com.talk_space.modules.users.api;

import com.talk_space.shared.error.CustomExceptions;
import com.talk_space.modules.users.api.model.Location;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.LocationDto;
import com.talk_space.modules.users.internal.persistence.LocationRepository;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public LocationService(LocationRepository locationRepository,
                           UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Location updateUserLocation(String userName, LocationDto locationDto) {
        logger.debug("Updating location for user: {}", userName);

        // Validate required fields
        if (locationDto.getCountry() == null || locationDto.getCountry().isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }

        if (locationDto.getRegion() == null || locationDto.getRegion().isBlank()) {
            throw new IllegalArgumentException("Region is required");
        }

        // Validate coordinates if provided
        if (locationDto.getLatitude() != null) {
            if (locationDto.getLatitude() < -90 || locationDto.getLatitude() > 90) {
                throw new IllegalArgumentException("Invalid latitude value. Must be between -90 and 90");
            }
        }
        if (locationDto.getLongitude() != null) {
            if (locationDto.getLongitude() < -180 || locationDto.getLongitude() > 180) {
                throw new IllegalArgumentException("Invalid longitude value. Must be between -180 and 180");
            }
        }

        Optional<User> userOptional = userRepository.findUserByUserName(userName);
        if (userOptional.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found: " + userName);
        }

        User user = userOptional.get();
        Optional<Location> existingLocation = locationRepository.findByUserUserName(userName);
        Location location;

        if (existingLocation.isPresent()) {
            location = existingLocation.get();
            logger.debug("Found existing location for user: {}", userName);
        } else {
            location = new Location();
            location.setUser(user);
            logger.debug("Creating new location for user: {}", userName);
        }

        location.setCountry(locationDto.getCountry().trim());
        location.setRegion(locationDto.getRegion() != null ? locationDto.getRegion().trim() : null);
        location.setCity(locationDto.getCity() != null ? locationDto.getCity().trim() : null);
        location.setVillage(locationDto.getVillage() != null ? locationDto.getVillage().trim() : null);
        location.setPlaceId(locationDto.getPlaceId() != null ? locationDto.getPlaceId().trim() : null);
        location.setFormattedAddress(locationDto.getFormattedAddress() != null ?
                locationDto.getFormattedAddress().trim() : null);
        location.setLatitude(locationDto.getLatitude());
        location.setLongitude(locationDto.getLongitude());

        Location savedLocation = locationRepository.save(location);
        logger.info("Location {} for user: {}",
                existingLocation.isPresent() ? "updated" : "created", userName);
        return savedLocation;
    }

    public Location getUserLocation(String userName) {
        logger.debug("Getting location for user: {}", userName);
        return locationRepository.findByUserUserName(userName)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException(
                        "Location not found for user: " + userName));
    }

    @Transactional
    public void deleteUserLocation(String userName) {
        logger.debug("Deleting location for user: {}", userName);
        Optional<Location> existingLocation = locationRepository.findByUserUserName(userName);
        if (existingLocation.isEmpty()) {
            logger.warn("No location found to delete for user: {}", userName);
            return;
        }
        locationRepository.deleteByUserUserName(userName);
        logger.info("Location deleted for user: {}", userName);
    }
}