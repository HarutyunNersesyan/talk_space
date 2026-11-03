package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.Location;
import com.talk_space.model.dto.LocationDto;
import com.talk_space.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public/location")
public class LocationController {
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    private final com.talk_space.connections.NetworkAccess access;

    public LocationController(LocationService locationService,com.talk_space.connections.NetworkAccess access) {
        this.access=access;
        this.locationService = locationService;
    }

    @PostMapping("/update/{userName}")
    public ResponseEntity<?> updateLocation(
            @PathVariable String userName,
            @Valid @RequestBody LocationDto locationDto) {
        access.requireRestActor(userName);
        logger.debug("Updating location for user: {}", userName);
        logger.debug("Location data: country={}, region={}, city={}, village={}, lat={}, lng={}",
                locationDto.getCountry(), locationDto.getRegion(), locationDto.getCity(),
                locationDto.getVillage(), locationDto.getLatitude(), locationDto.getLongitude());

        try {
            Location location = locationService.updateUserLocation(userName, locationDto);
            logger.info("Location updated successfully for user: {}", userName);
            return ResponseEntity.ok(location);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating location for user {}: {}", userName, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating location for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update location: " + e.getMessage());
        }
    }

    @GetMapping("/{userName}")
    public ResponseEntity<?> getLocation(@PathVariable String userName) {
        access.requireCurrentVisible(userName);
        logger.debug("Getting location for user: {}", userName);

        try {
            Location location = locationService.getUserLocation(userName);
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            logger.error("Error getting location for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Location not found for user: " + userName);
        }
    }

    @DeleteMapping("/{userName}")
    public ResponseEntity<?> deleteLocation(@PathVariable String userName) {
        access.requireRestActor(userName);
        logger.debug("Deleting location for user: {}", userName);

        try {
            locationService.deleteUserLocation(userName);
            return ResponseEntity.ok("Location deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting location for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete location: " + e.getMessage());
        }
    }
}