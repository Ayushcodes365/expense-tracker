package com.example.expensetracker.service;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.SummaryResponse;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Transactional
public class ExpenseService {

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "date")
            .and(Sort.by(Sort.Direction.DESC, "id"));

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Expense> list(Category category, LocalDate from, LocalDate to) {
        return repository.findAll(filter(category, from, to), NEWEST_FIRST);
    }

    @Transactional(readOnly = true)
    public Expense get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Expense create(ExpenseRequest request) {
        Expense expense = new Expense();
        apply(expense, request);
        return repository.save(expense);
    }

    public Expense update(Long id, ExpenseRequest request) {
        Expense expense = get(id);
        apply(expense, request);
        return repository.save(expense);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary(LocalDate from, LocalDate to) {
        List<Expense> expenses = repository.findAll(filter(null, from, to));

        BigDecimal total = BigDecimal.ZERO;
        Map<Category, BigDecimal> byCategory = new EnumMap<>(Category.class);
        Map<String, BigDecimal> byMonth = new TreeMap<>();

        for (Expense e : expenses) {
            total = total.add(e.getAmount());
            byCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
            byMonth.merge(e.getDate().format(MONTH), e.getAmount(), BigDecimal::add);
        }
        return new SummaryResponse(total, expenses.size(), byCategory, byMonth);
    }

    private void apply(Expense expense, ExpenseRequest request) {
        expense.setTitle(request.title().trim());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setDate(request.date());
        expense.setNotes(request.notes());
    }

    private Specification<Expense> filter(Category category, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
