package ru.practicum.service.impl;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

public interface CommentPrivateService {
    /**
     * Adds a new comment to an event by a specific user.
     *
     * @param userId        the ID of the user adding the comment
     * @param eventId       the ID of the event to which the comment is being added
     * @param newCommentDto the data transfer object containing the comment's details
     * @return the added CommentDto containing the newly created comment's information
     */
    CommentDto addCommentDto(Long userId, Long eventId, NewCommentDto newCommentDto);

    /**
     * Updates an existing comment by a specific user.
     *
     * @param userId        the ID of the user updating the comment
     * @param commentId     the ID of the comment to be updated
     * @param newCommentDto the data transfer object containing the new details of the comment
     * @return the updated CommentDto containing the updated comment's information
     */
    CommentDto updateCommentDto(Long userId, Long commentId, NewCommentDto newCommentDto);

    /**
     * Deletes a comment by a specific user.
     *
     * @param userId    the ID of the user deleting the comment
     * @param commentId the ID of the comment to be deleted
     */
    void deleteCommentDto(Long userId, Long commentId);
}
