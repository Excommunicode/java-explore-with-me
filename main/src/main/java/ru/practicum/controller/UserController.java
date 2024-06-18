package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.impl.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto saveUser(@Valid @RequestBody UserDto userDto) {
        System.err.println("Attempting to create a new user");
        UserDto createdUser = userService.addUserDto(userDto);
        System.err.println("Created user with ID: " + createdUser.getId());
        return createdUser;
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(value = "ids", required = false) Long id, @RequestParam(defaultValue = "0") Integer from,
                                  @RequestParam(defaultValue = "10") Integer size) {
        System.err.println("Fetching users starting from index " + from + " with a page size of " + size);
        List<UserDto> users = userService.getUsers(id, from, size);
        System.err.println("Fetched " + users.size() + " users starting from index " + from);
        return users;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserDto(@PathVariable Long userId) {
        System.err.println("Attempting to delete user with ID: " + userId);
        userService.deleteUserDto(userId);
        System.err.println("Deleted user with ID: " + userId);
    }
}
