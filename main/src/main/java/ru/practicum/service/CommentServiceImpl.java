package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.enums.CommentSort;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.api.CommentAdminService;
import ru.practicum.service.api.CommentPrivateService;
import ru.practicum.service.api.CommentPublicService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class CommentServiceImpl implements CommentPrivateService, CommentPublicService, CommentAdminService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EntityManager entityManager;


    @Transactional
    @Override
    public CommentDto addCommentDto(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.debug("Adding comment for userId: {} and eventId: {}", userId, eventId);
        existsUserById(userId);
        exestsEventById(eventId);
        CommentDto commentDto = commentMapper.toDto(commentRepository.save(
                commentMapper.toModel(userId, eventId, newCommentDto, LocalDateTime.now())));
        log.info("Comment added successfully for userId: {} and eventId: {}", userId, eventId);
        return commentDto;
    }

    @Transactional
    @Override
    public CommentDto updateCommentDto(Long userId, Long commentId, NewCommentDto newCommentDto) {
        log.debug("Updating comment for userId: {} and commentId: {}", userId, commentId);
        existsUserById(userId);

//        CommentDto commentDto = findCommentById(commentId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Not found"));
        checkIsAuthor(userId, comment.getAuthor().getId());

        return commentMapper.toDto(commentMapper.updateComment(comment, newCommentDto));

//        commentRepository.updateById_updAndUpdatedAndText(commentId, newCommentDto.getText());
//
//        entityManager.clear();
//
//        CommentDto updatedComment = findCommentById(commentId);
//        log.info("Comment updated successfully for userId: {} and commentId: {}", userId, commentId);
//        return updatedComment;
    }

    @Transactional
    @Override
    public void deleteCommentDto(Long userId, Long commentId) {
        log.debug("Deleting comment for userId: {} and commentId: {}", userId, commentId);
        CommentDto commentDto = findCommentById(commentId);
        checkIsAuthor(userId, commentDto.getAuthor());
        commentRepository.deleteById(commentId);
        log.info("Comment deleted successfully for userId: {} and commentId: {}", userId, commentId);
    }

    @Override
    public List<CommentDto> findCommentsDtoById(Long eventId, CommentSort commentSort, int from, int size) {
        log.debug("Finding comments for eventId: {} with sort: {} from: {} size: {}", eventId, commentSort, from, size);
        Pageable pageable = null;
        if (commentSort != null) {
            switch (commentSort) {
                case SORT_DATE_DESC:
                    pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));
                    break;
                case SORT_DATE_ASC:
                    pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "created"));
            }
        } else {
            pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "id"));
        }
        List<CommentDto> commentsFromDb = commentMapper.toListDto(commentRepository.findAllByEvent_Id(eventId, pageable));
        if (commentsFromDb.isEmpty()) {
            log.info("No comments found for eventId: {}", eventId);
            return Collections.emptyList();
        }
        log.info("Found {} size  comments for event: {}", commentsFromDb.size(), eventId);
        return commentsFromDb;
    }

    @Override
    public int countCommentsByEventId(Long eventId) {
        log.debug("Counting comments for eventId: {}", eventId);

        int count = commentRepository.countByEventId(eventId);

        log.info("Found {} comments for eventId: {}", count, eventId);
        return count;
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {

        eventRepository.deleteById(commentId);
    }

    private void existsUserById(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }

    }

    private void exestsEventById(Long eventId) {

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException(String.format("Event with id %s not found", eventId));
        }

    }

    private CommentDto findCommentById(Long commentId) {

        return commentMapper.toDto(commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id %s not found", commentId))));
    }

    private void checkIsAuthor(Long userId, Long commentAuthorId) {

        if (!Objects.equals(userId, commentAuthorId)) {
            throw new ConflictException(String.format("User with id %s is not author for comment", userId));
        }

    }

}
