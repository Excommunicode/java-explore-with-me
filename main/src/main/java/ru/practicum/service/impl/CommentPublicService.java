package ru.practicum.service.impl;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.enums.CommentSort;

import java.util.List;

public interface CommentPublicService {
    /**
     * Retrieves a list of comments associated with a specific event, sorted according to the specified criteria.
     *
     * @param eventId     the ID of the event for which comments are to be retrieved
     * @param commentSort the sorting criteria for the comments
     * @param from        the starting index from which to retrieve comments
     * @param size        the maximum number of comments to retrieve
     * @return a list of CommentDto containing the comments for the event
     */
    List<CommentDto> findCommentsDtoById(Long eventId, CommentSort commentSort, int from, int size);

    /**
     * Counts the total number of comments associated with a specific event.
     *
     * @param eventId the ID of the event for which to count the comments
     * @return the total number of comments associated with the event
     */
    int countCommentsByEventId(Long eventId);
}
