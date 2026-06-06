package com.hotel.aichat.controller;

import com.hotel.aichat.dto.ChatRequest;
import com.hotel.aichat.dto.ChatResponse;
import com.hotel.aichat.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String answer =
                chatService.ask(request.message());

        return new ChatResponse(answer);
    }
}
