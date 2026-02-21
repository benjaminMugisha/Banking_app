package com.benjamin.Banking_app.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminServiceImpl adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
