package com.talk_space;
import com.talk_space.service.ImageService;
import com.talk_space.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.Path;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class ImageServicePathTest {
    @TempDir Path directory;
    ImageService service() {
        var service=new ImageService(mock(ImageRepository.class),mock(UserRepository.class));
        ReflectionTestUtils.setField(service,"imageDirectory",directory.toString());return service;
    }
    @Test void imagesResolveInsideConfiguredDirectory() {
        assertEquals(directory.resolve("photo.png"),service().resolveImagePath("photo.png"));
    }
    @Test void traversalOutsideConfiguredDirectoryIsRejected() {
        assertThrows(IllegalArgumentException.class,()->service().resolveImagePath("../photo.png"));
    }
}
