package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    Compilation toModel(UpdateCompilationRequest updateCompilationRequest, Set<Event> events);


    Compilation toModel(CompilationDto compilationDto);


                        CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toListDto(List<Compilation> compilations);

    @Mapping(target = "events", source = "events")
    Compilation toModelAfterUpdate(CompilationDto compilationDto, Set<Event> events);
}
