package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
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


        public void addImage(ImageDto imageDto) throws CustomExceptions.ImageLimitExceededException {
            Optional<User> userOptional = userRepository.findUserByUserName(imageDto.getUserName());

            if (userOptional.isEmpty()) {
                throw new CustomExceptions.UserNotFoundException("User not found");
            }

            User user = userOptional.get();
            List<Image> imageList = imageDto.getImages();

            if (user.getImages().size() + imageList.size() > 5) {
                throw new CustomExceptions.ImageLimitExceededException("The number of images cannot exceed 5");
            }

            imageList.forEach(image -> image.setUser(user));
            user.getImages().addAll(imageList);
            imageRepository.saveAll(imageList);
            userRepository.save(user);
        }




    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }
}
