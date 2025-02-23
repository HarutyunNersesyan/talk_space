package com.talk_space.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeleteImageRequest {
    private String userName;
    private String fileName;
}
