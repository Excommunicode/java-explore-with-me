package ru.practicum.service.impl;

import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUserDto(UserDto userDto);

    List<UserDto> getUsers(Long id, int from, int size);

    void deleteUserDto(Long id);
}
