package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.impl.CommentPrivateService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/comments")
public class CommentPrivateController {
    private final CommentPrivateService commentPrivateService;


    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addCommentDto(@PathVariable Long userId, @PathVariable Long eventId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentPrivateService.addCommentDto(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentDto(@PathVariable Long userId, @PathVariable Long commentId,
                                       @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentPrivateService.updateCommentDto(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentDto(@PathVariable Long userId, @PathVariable Long commentId) {
        commentPrivateService.deleteCommentDto(userId, commentId);
    }
}