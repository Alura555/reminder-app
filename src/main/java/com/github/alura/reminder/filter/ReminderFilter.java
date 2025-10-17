package com.github.alura.reminder.filter;

import com.github.alura.reminder.entity.Reminder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReminderFilter implements Specification<Reminder> {
    private String search;
    private Long userId;
    private LocalDateTime from;
    private LocalDateTime to;

    @Override
    public Predicate toPredicate(Root<Reminder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (userId != null && userId != 0L) {
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
        }

        if (search != null) {
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + search.toLowerCase() + "%")
            ));
        }

        if (from!= null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("remind"), from));
        }
        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("remind"), to));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
