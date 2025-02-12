package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ImageDto;
import com.talk_space.repository.ImageRepository;
import com.talk_space.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;



//        public static void main(String[] args) throws IOException {
//            File testFile = new File("C:/Users/HARUT_037/Desktop/talk_space/images/test.txt");
//            try (FileOutputStream fos = new FileOutputStream(testFile)) {
//                fos.write("test".getBytes());
//            }
//            System.out.println("File created successfully at " + testFile.getAbsolutePath());
//        }



    private static final String IMAGE_DIR = "C:/Users/HARUT_037/Desktop/talk_space/images/";

    @Transactional
    public void updateImages(ImageDto imageDto) {
        Optional<User> userOptional = userRepository.findUserByUserName(imageDto.getUserName());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();

        List<Image> existingImages = imageRepository.findByUser(user);
        if (existingImages.size() >= 3) {
            throw new RuntimeException("User already has 3 images. Cannot upload more.");
        }

        File directory = new File(IMAGE_DIR);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + IMAGE_DIR);
        }

        List<Image> newImages = imageDto.getImages().stream().map(imageData -> {
            try {
                System.out.println("Processing image: " + imageData.getFileName());

                if (!isValidFileType(imageData.getFileType())) {
                    throw new IllegalArgumentException("Invalid file type: " + imageData.getFileType() +
                            ". Only JPEG and PNG are allowed.");
                }

                if (existingImages.size() >= 3) {
                    throw new RuntimeException("User already has 3 images. Cannot upload more.");
                }

                byte[] imageBytes = Base64.getDecoder().decode(imageData.getData());
                String uniqueFileName = System.currentTimeMillis() +
                        (imageData.getFileType().equals("image/png") ? ".png" : ".jpeg");

                String filePath = IMAGE_DIR + uniqueFileName;

                File imageFile = new File(filePath);
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(imageBytes);
                }

                Image image = new Image();
                image.setFileName(uniqueFileName);
                image.setFileType(imageData.getFileType());
                image.setFilePath(filePath);
                image.setUser(user);

                System.out.println("Successfully saved image: " + filePath);
                existingImages.add(image); // Update count for next iterations
                return image;

            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to save image: " + imageData.getFileName(), e);
            }
        }).toList();

        imageRepository.saveAll(newImages);
    }



    private boolean isValidFileType(String fileType) {
        return "image/jpeg".equalsIgnoreCase(fileType) || "image/png".equalsIgnoreCase(fileType);
    }

    public List<String> getUserImages(String userName) {
        Optional<User> userOptional = userRepository.findUserByUserName(userName);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return imageRepository.findByUser(userOptional.get()).stream()
                .map(Image::getFilePath)
                .toList();
    }

}

