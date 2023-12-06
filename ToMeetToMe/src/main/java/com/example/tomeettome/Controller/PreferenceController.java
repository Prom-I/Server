package com.example.tomeettome.Controller;

import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.ParserException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/preference")
public class PreferenceController {

    @PutMapping("/create/{icsFileName}")
    public ResponseEntity<?> create(@AuthenticationPrincipal String userId,
                                    @PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String component) throws ParserException, IOException {

    }
}
