package com.talk_space.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
    public class ImageData {
    private String fileName;
    private String fileType;
    private String data;

}
