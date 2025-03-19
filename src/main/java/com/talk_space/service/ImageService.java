package com.talk_space.service;


import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ImageDto;
import com.talk_space.repository.ImageRepository;
import com.talk_space.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    private static final String IMAGE_DIR = "C:/Users/HARUT_037/Desktop/talk_space/images/";

    @Transactional
    public String uploadProfilePicture(MultipartFile file, String userName) {
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Image> existingImages = imageRepository.findByUser(user);
        if (existingImages.size() >= 3) {
            throw new RuntimeException("User already has 3 images. Cannot upload more.");
        }

        ensureDirectoryExists();

        if (!isValidFileType(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type: " + file.getContentType() +
                    ". Only JPEG and PNG are allowed.");
        }

        String fileName = generateUniqueFileName(file.getContentType());
        String filePath = IMAGE_DIR + fileName;
        storeFile(file, filePath);

        Image image = new Image();
        image.setFileName(fileName);
        image.setFileType(file.getContentType());
        image.setFilePath(filePath);
        image.setUser(user);

        imageRepository.save(image);
        return filePath;
    }

    private void ensureDirectoryExists() {
        File directory = new File(IMAGE_DIR);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + IMAGE_DIR);
        }
    }

    private String generateUniqueFileName(String fileType) {
        return System.currentTimeMillis() + (fileType.equals("image/png") ? ".png" : ".jpeg");
    }

    private void storeFile(MultipartFile file, String filePath) {
        try {
            File imageFile = new File(filePath);
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image file", e);
        }
    }

    private boolean isValidFileType(String fileType) {
        return "image/jpeg".equalsIgnoreCase(fileType) || "image/png".equalsIgnoreCase(fileType);
    }

    public List<String> getUserImages(String userName) {
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return imageRepository.findByUser(user).stream()
                .map(Image::getFilePath)
                .toList();
    }

    @Transactional
    public void deleteImage(String userName, String fileName) {
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Image image = imageRepository.findByUserAndFileName(user, fileName)
                .orElseThrow(() -> new IllegalArgumentException("Image not found for user"));

        deleteImageFile(image.getFilePath());

        imageRepository.delete(image);
    }

    private void deleteImageFile(String filePath) {
        File imageFile = new File(filePath);
        if (imageFile.exists() && !imageFile.delete()) {
            throw new RuntimeException("Failed to delete image file: " + filePath);
        }
    }
}


