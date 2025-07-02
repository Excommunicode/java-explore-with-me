package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * Updates the text of a comment and sets its 'updated' flag to true based on the comment's ID.
     *
     * @param commentId the ID of the comment to be updated.
     * @param text      the new text to replace the existing comment text.
     */
    @Modifying
    @Query("UPDATE Comment c SET c.text = :text, c.updated = true  WHERE c.id = :commentId")
    void updateById_updAndUpdatedAndText(Long commentId, String text);

    /**
     * Retrieves all comments associated with a specific event, with pagination.
     *
     * @param eventId  the ID of the event for which comments are being retrieved.
     * @param pageable the pagination information (page number, page size, sorting directions).
     * @return a pageable list of {@link Comment} objects associated with the event.
     */
    @Query("SELECT c " +
            "FROM Comment c " +
            "JOIN FETCH c.event e " +
            "WHERE e.id = :eventId")
    List<Comment> findAllByEvent_Id(Long eventId, Pageable pageable);

    /**
     * Counts the total number of comments associated with a specific event.
     *
     * @param eventId the ID of the event for which to count the comments.
     * @return the total number of comments associated with the event.
     */
    @Query("SELECT COUNT(c) " +
            "FROM Comment c " +
            "WHERE c.id = :eventId")
    int countByEventId(Long eventId);
}