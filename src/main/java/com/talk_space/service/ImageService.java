package com.talk_space.service;

import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.User;
import com.talk_space.repository.ImageRepository;
import com.talk_space.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${app.images.directory:C:/Users/HARUT_037/Desktop/talk_space/images/}")
    private String imageDirectory;

    public java.nio.file.Path resolveImagePath(String fileName) {
        var root = java.nio.file.Paths.get(imageDirectory).toAbsolutePath().normalize();
        var path = root.resolve(fileName).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid image path");
        return path;
    }

    @Transactional
    public String uploadProfilePicture(MultipartFile file, String userName) {
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Image> existingImage = imageRepository.findByUserUserName(userName);
        if (existingImage.isPresent()) {

            deleteImageFile(existingImage.get().getFilePath());
            imageRepository.deleteById(existingImage.get().getId());
            imageRepository.flush();
        }
        ensureDirectoryExists();
        if (!isValidFileType(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type: " + file.getContentType() +
                    ". Only JPEG and PNG are allowed.");
        }

        String fileName = generateUniqueFileName(Objects.requireNonNull(file.getContentType()));
        String filePath = resolveImagePath(fileName).toString();
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
        File directory = new File(imageDirectory);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + imageDirectory);
        }
    }

    private String generateUniqueFileName(String fileType) {
        return System.currentTimeMillis() + "_" + UUID.randomUUID() +
                (fileType.equals("image/png") ? ".png" : ".jpeg");
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

    public String getUserImage(String userName) {
        Image image = imageRepository.findByUserUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("No image found for user"));
        return image.getFileName();
    }

    @Transactional
    public void deleteUserImage(String userName) {
        Image image = imageRepository.findByUserUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("No image found for user"));
        image.getUser().setImage(null);
        deleteImageFile(image.getFilePath());
        imageRepository.delete(image);
    }

    public void deleteImageFile(String filePath) {
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            if (imageFile.delete()) {
                System.out.println("Deleted file: " + filePath);
            } else {
                System.out.println("Failed to delete file: " + filePath);
            }
        } else {
            System.out.println("File not found: " + filePath);
        }
    }

}