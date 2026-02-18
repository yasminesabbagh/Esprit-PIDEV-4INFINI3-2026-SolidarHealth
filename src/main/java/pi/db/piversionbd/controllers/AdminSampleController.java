package pi.db.piversionbd.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sample")
public class AdminSampleController {

    @GetMapping
    public ResponseEntity<String> onlyAdmin() {
        return ResponseEntity.ok("ADMIN access OK");
    }
}

