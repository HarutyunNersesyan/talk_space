package com.talk_space.service;


import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ImageDto;
import com.talk_space.repository.ImageRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    private final UserRepository userRepository;

    public ResponseEntity<String> addImage(ImageDto imageDto) {
        Optional<User> userOptional = userRepository.findUserByUserName(imageDto.getUserName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOptional.get();
        List<Image> imageList = imageDto.getImages();

        if (user.getImages().size() + imageList.size() > 5) {
            return ResponseEntity.badRequest().body("The number of images cannot exceed 5");
        }
        imageList.forEach(image -> image.setUser(user));
        user.getImages().addAll(imageList);
        imageRepository.saveAll(imageList);
        userRepository.save(user);

        return ResponseEntity.ok("Images updated successfully");
    }


    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }
}
