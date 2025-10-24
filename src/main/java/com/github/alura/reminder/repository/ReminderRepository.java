package com.github.alura.reminder.repository;

import com.github.alura.reminder.entity.Reminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long>, JpaSpecificationExecutor<Reminder> {
    Page<Reminder> findAll(Specification<Reminder> filter, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Reminder> findAllByRemindBeforeAndIsSentFalse(LocalDateTime now);
}
