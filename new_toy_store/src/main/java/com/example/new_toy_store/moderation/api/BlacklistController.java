package com.example.new_toy_store.moderation.api;

import com.example.new_toy_store.moderation.application.BlacklistWordService;
import com.example.new_toy_store.moderation.application.dto.request.BlacklistedWordFilterRequest;
import com.example.new_toy_store.moderation.application.dto.request.BlacklistedWordRequest;
import com.example.new_toy_store.moderation.application.dto.response.BlacklistedWordResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/moderation/blacklists")
@Validated
public class BlacklistController {

    private final BlacklistWordService service;

    public BlacklistController(BlacklistWordService service) {
        this.service = service;
    }

    @GetMapping
    public Page<BlacklistedWordResponse> searchWords(@Valid @ModelAttribute BlacklistedWordFilterRequest filter, Pageable pageable) {
        return service.searchWords(filter, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addWord(@Valid @RequestBody BlacklistedWordRequest request) {
        service.addWord(request.getWord(), request.getCategory());
    }

    @PutMapping("/{id}")
    public void updateWord(@PathVariable Integer id, @Valid @RequestBody BlacklistedWordRequest request) {
        service.updateWord(id, request.getWord(), request.getCategory());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteWord(@PathVariable Integer id) {
        service.softDeleteWord(id);
    }

    @PutMapping("/{id}/restore")
    public void restoreWord(@PathVariable Integer id) {
        service.restoreWord(id);
    }

    @DeleteMapping("/{id}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDeleteWord(@PathVariable Integer id) {
        service.hardDeleteWord(id);
    }
}