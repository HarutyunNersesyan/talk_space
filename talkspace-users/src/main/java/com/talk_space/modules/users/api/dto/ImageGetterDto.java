package com.talk_space.modules.users.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class ImageGetterDto {

    private String fileType;
    private String filePath;


}
