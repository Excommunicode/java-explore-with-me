package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEvent_Initiator_IdAndAndEvent_id(Long userId, Long eventId);

    List<ParticipationRequest> findAllByRequester_Id(Long userId);

    boolean existsByRequester_IdAndEvent_id(Long userId, Long eventId);

    List<ParticipationRequest> findAllByEvent_IdInAndAndStatus(List<Long> eventIds, ParticipationRequestStatus participationRequest);

    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE participation_requests SET status = :status WHERE id = :id")
    void updateByIdStatus(Long id, String status);

    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM participation_requests e " +
                    "WHERE id IN :requestIds")
    List<ParticipationRequest> findAllByIds(List<Long> requestIds);

    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE participation_requests  SET status = :status WHERE id IN :participationIds")
    void updateByIdIn(@Param("participationIds") List<Long> participationIds, @Param("status") String status);
}