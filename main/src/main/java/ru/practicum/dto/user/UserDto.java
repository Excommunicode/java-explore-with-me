package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.util.NotOnlySpaces;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {

    private Long id;

    @NotOnlySpaces
    @NotNull(message = "The field name must not be null")
    @Size(min = 2, max = 250, message = "Name must be between 2 and 250 characters long")
    private String name;

    @NotOnlySpaces
    @Email(message = "Invalid email address format")
    @NotEmpty(message = "Email address cannot be empty")
    @Size(min = 6, max = 254, message = "Email address must be between 6 and 254 characters long")
    private String email;
}
