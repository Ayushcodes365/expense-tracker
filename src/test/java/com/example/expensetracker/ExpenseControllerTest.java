package com.example.expensetracker;

import com.example.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ExpenseRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private static String json(String title, String amount, String category, String date) {
        return """
                {"title":"%s","amount":%s,"category":"%s","date":"%s"}
                """.formatted(title, amount, category, date);
    }

    private void create(String title, String amount, String category, String date) throws Exception {
        mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
                        .content(json(title, amount, category, date)))
                .andExpect(status().isCreated());
    }

    @Test
    void createsAndFetchesExpense() throws Exception {
        mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Lunch", "12.50", "FOOD", "2026-10-01")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title", is("Lunch")));

        mvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void rejectsInvalidExpense() throws Exception {
        mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "-5", "FOOD", "2026-10-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void updatesAndDeletes() throws Exception {
        create("Bus", "2.00", "TRANSPORT", "2026-10-02");
        long id = repository.findAll().get(0).getId();

        mvc.perform(put("/api/expenses/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Metro", "3.00", "TRANSPORT", "2026-10-02")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Metro")));

        mvc.perform(delete("/api/expenses/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/expenses/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void filtersByCategoryAndDate() throws Exception {
        create("Lunch", "10.00", "FOOD", "2026-09-15");
        create("Dinner", "20.00", "FOOD", "2026-10-05");
        create("Train", "5.00", "TRANSPORT", "2026-10-06");

        mvc.perform(get("/api/expenses").param("category", "FOOD"))
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/expenses").param("from", "2026-10-01"))
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/expenses").param("category", "FOOD").param("from", "2026-10-01"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void summarizesTotals() throws Exception {
        create("Lunch", "10.00", "FOOD", "2026-09-15");
        create("Dinner", "20.00", "FOOD", "2026-10-05");
        create("Train", "5.00", "TRANSPORT", "2026-10-06");

        mvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(35.00)))
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.byCategory.FOOD", is(30.00)))
                .andExpect(jsonPath("$.byMonth['2026-10']", is(25.00)));
    }
}
