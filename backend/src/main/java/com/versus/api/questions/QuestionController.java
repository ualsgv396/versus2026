package com.versus.api.questions;

import com.versus.api.questions.dto.QuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/random")
    public QuestionResponse random(@RequestParam(required = false) QuestionType type,
                                   @RequestParam(required = false) String category) {
        return questionService.getRandom(type, category);
    }

    @GetMapping("/{id}")
    public QuestionResponse byId(@PathVariable UUID id) {
        return questionService.getById(id);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return questionService.getCategories();
    }
}
