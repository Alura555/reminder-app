package com.github.alura.reminder.filter;

import com.github.alura.reminder.entity.Reminder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReminderFilter implements Specification<Reminder> {
    private String search;
    private Long userId;

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

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
