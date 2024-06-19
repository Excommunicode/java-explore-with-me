package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.impl.UserAdminService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.constant.UserConstant.INITIAL_X;
import static ru.practicum.constant.UserConstant.LIMIT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
public class UserAdminController {
    private final UserAdminService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto saveUser(@Valid @RequestBody UserDto userDto) {
        return userService.addUserDto(userDto);
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(value = "ids", required = false) Long id,
                                  @RequestParam(defaultValue = INITIAL_X) int from,
                                  @RequestParam(defaultValue = LIMIT) int size) {
        return userService.getUsers(id, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserDto(@PathVariable Long userId) {
        userService.deleteUserDto(userId);
    }
}
