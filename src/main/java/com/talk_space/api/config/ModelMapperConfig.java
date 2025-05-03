package com.talk_space.api.config;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.dto.ChatMessageDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(ChatMessage.class, ChatMessageDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getSender().getUserName(), ChatMessageDto::setSender);
                    mapper.map(src -> src.getSender().getFirstName() + " " + src.getSender().getLastName(),
                            ChatMessageDto::setSenderName);
                    mapper.map(src -> src.getReceiver().getUserName(), ChatMessageDto::setReceiver);
                    mapper.map(src -> src.getReceiver().getFirstName() + " " + src.getReceiver().getLastName(),
                            ChatMessageDto::setReceiverName);
                });

        return modelMapper;
    }
}
