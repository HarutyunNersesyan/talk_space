package com.talk_space.service;


import com.talk_space.model.domain.Image;
import com.talk_space.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public Image addImage(Image image){
       return imageRepository.save(image);
    }

    public void deleteImage(Long id){
        imageRepository.deleteById(id);
    }
}
