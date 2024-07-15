package ru.practicum.service.api;

import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserAdminService {
    /**
     * Adds a new user to the system based on the provided UserDto.
     *
     * @param userDto the data transfer object containing the user's details
     * @return the UserDto containing details of the newly added user
     */
    UserDto addUserDto(UserDto userDto);

    /**
     * Retrieves a list of users, with pagination support.
     *
     * @param id   the ID used as a starting point for fetching users (may be used for sorting or filtering)
     * @param from the starting index from which to retrieve users
     * @param size the maximum number of users to retrieve
     * @return a list of UserDto representing the users fetched from the system
     */
    List<UserDto> getUsers(List<Long> ids, int from, int size);

    /**
     * Deletes a user from the system identified by their ID.
     *
     * @param id the ID of the user to be deleted
     */
    void deleteUserDto(Long id);
}
