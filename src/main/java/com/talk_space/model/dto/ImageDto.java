package com.talk_space.model.dto;


import com.talk_space.model.domain.Image;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class ImageDto {

    @NotNull(message = "User name cannot be null")
    private String userName;

    private List<MultipartFile> images;  // Changed to MultipartFile for image upload
}

