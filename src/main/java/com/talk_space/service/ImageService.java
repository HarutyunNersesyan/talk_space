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

        validatePhoto(file);

        Optional<Image> existingImage = imageRepository.findByUserUserName(userName);
        if (existingImage.isPresent()) {

            deleteImageFile(existingImage.get().getFilePath());
            imageRepository.deleteById(existingImage.get().getId());
            imageRepository.flush();
        }
        ensureDirectoryExists();

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

    private void validatePhoto(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > 5L * 1024 * 1024)
            throw new IllegalArgumentException("Choose an image up to 5 MB.");
        if (!isValidFileType(file.getContentType()))
            throw new IllegalArgumentException("Only JPG and PNG images are allowed.");
        try (var input = javax.imageio.ImageIO.createImageInputStream(file.getInputStream())) {
            if (input == null) throw new IllegalArgumentException("The image could not be opened.");
            var readers = javax.imageio.ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("The image could not be opened.");
            var reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName();
                boolean matchingFormat = ("image/png".equalsIgnoreCase(file.getContentType()) && "png".equalsIgnoreCase(format))
                        || ("image/jpeg".equalsIgnoreCase(file.getContentType()) && ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)));
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (!matchingFormat) throw new IllegalArgumentException("The file contents do not match the image type.");
                if (width < 400 || height < 300 || width > 10000 || height > 10000 || (long) width * 3 != (long) height * 4)
                    throw new IllegalArgumentException("The profile photo must be 4:3 and at least 400 × 300 pixels.");
            } finally { reader.dispose(); }
        } catch (IOException ex) {
            throw new IllegalArgumentException("The image could not be opened.", ex);
        }
    }

    public Optional<String> getUserImage(String userName) {
        return imageRepository.findByUserUserName(userName).map(Image::getFileName);
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
