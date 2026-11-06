package com.talk_space;

import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.internal.persistence.ImageRepository;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import com.talk_space.modules.users.api.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageUploadRulesTest {
    @TempDir Path directory;
    private MockMultipartFile image(int width, int height) throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return new MockMultipartFile("file", "photo.png", "image/png", bytes.toByteArray());
    }
    @Test void validatesBeforeDeletingExistingImageAndAcceptsValidCrop() throws Exception {
        var images = mock(ImageRepository.class);
        var users = mock(UserRepository.class);
        when(users.findUserByUserName("alice")).thenReturn(Optional.of(new User()));
        var service = new ImageService(images, users);
        ReflectionTestUtils.setField(service, "imageDirectory", directory.toString());
        assertThrows(IllegalArgumentException.class, () -> service.uploadProfilePicture(image(400, 400), "alice"));
        assertThrows(IllegalArgumentException.class, () -> service.uploadProfilePicture(
            new MockMultipartFile("file", "bad.png", "image/png", new byte[]{1,2,3}), "alice"));
        verifyNoInteractions(images);
        String path = service.uploadProfilePicture(image(800, 600), "alice");
        assertTrue(java.nio.file.Files.exists(Path.of(path)));
        verify(images).save(any());
    }
}
