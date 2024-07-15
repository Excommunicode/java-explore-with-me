package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.api.UserAdminService;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class UserServiceImpl implements UserAdminService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDto addUserDto(UserDto userDto) {
        log.debug("Adding a user with email: {}", userDto.getEmail());

        checkExistsEmail(userDto.getEmail());
        UserDto savedUserDto = userMapper.toDto(userRepository.save(userMapper.toModel(userDto)));

        log.info("User added with ID: {}", savedUserDto.getId());
        return savedUserDto;
    }

    @Transactional
    @Override
    public void deleteUserDto(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        return ids != null ? findAllUsersDtoById(ids, pageable) : findAllUsersDto(pageable);
    }

    private List<UserDto> findAllUsersDtoById(List<Long> ids, Pageable pageable) {
        return userMapper.toDtoList(userRepository.findAllByIdIn(ids, pageable));
    }

    private List<UserDto> findAllUsersDto(Pageable pageable) {
        return userMapper.toDtoList(userRepository.findAll(pageable).getContent());
    }

    private void checkExistsEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(String.format("This email %s has already taken", email));
        }
    }
}