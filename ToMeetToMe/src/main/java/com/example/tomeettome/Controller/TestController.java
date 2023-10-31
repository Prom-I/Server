package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.ResponseDTO;
import com.example.tomeettome.DTO.TestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("test")
public class TestController {

    // TEST용 API
    @PostMapping
    public ResponseEntity<?> test(@RequestBody TestDTO dto) {

        String msg = dto.getOs() + ", Welcome to TMTM Server. What the HELL!";
        dto.setMsg(msg);

        ResponseDTO<TestDTO> response = ResponseDTO.<TestDTO>builder().data(Collections.singletonList(dto)).status("succeed").build();

        return ResponseEntity.ok().body(msg);
    }
}
