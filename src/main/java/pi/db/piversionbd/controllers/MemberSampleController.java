package pi.db.piversionbd.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/sample")
public class MemberSampleController {

    @GetMapping
    public ResponseEntity<String> onlyMember() {
        return ResponseEntity.ok("MEMBER access OK");
    }
}

