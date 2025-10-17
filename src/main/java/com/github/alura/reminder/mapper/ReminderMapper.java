package com.github.alura.reminder.mapper;

import com.github.alura.reminder.dto.ReminderDto;
import com.github.alura.reminder.dto.ReminderListRequest;
import com.github.alura.reminder.dto.ReminderRequestDto;
import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.filter.ReminderFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Mapper(componentModel = "spring")
public interface ReminderMapper {
    @Mapping(source = "user.id", target = "userId")
    ReminderDto toDto(Reminder reminder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Reminder toEntity(ReminderRequestDto reminderDto);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "and", ignore = true)
    @Mapping(target = "or", ignore = true)
    ReminderFilter toFilter(ReminderListRequest request, Long userId);

    void updateFromDto(ReminderRequestDto dto, @MappingTarget Reminder entity);

    default Pageable toPageable(ReminderListRequest request) {
        Sort sort = Sort.by(request.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC, request.getSortBy().getFieldName());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
}
