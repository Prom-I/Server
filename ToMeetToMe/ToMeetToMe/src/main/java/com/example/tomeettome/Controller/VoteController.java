package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.ResponseDTO;
import com.example.tomeettome.DTO.VoteDTO;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.VoteEntity;
import com.example.tomeettome.Service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/vote")
public class VoteController {
    @Autowired VoteService voteService;

    @PutMapping("/like")
    public ResponseEntity<?> like(@AuthenticationPrincipal String userId,
                                  @RequestBody VoteDTO dto) {
        try {
            voteService.like(userId, VoteDTO.toEntity(dto));
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PutMapping("/dislike")
    public ResponseEntity<?> dislike(@AuthenticationPrincipal String userId,
                                     @RequestBody VoteDTO dto) {
        try {
            voteService.dislike(userId, VoteDTO.toEntity(dto));
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve/{promiseUid}")
    public ResponseEntity<?> retrieveLikes(@AuthenticationPrincipal String userId,
                                           @PathVariable("promiseUid") String promiseUid) {
        try {
            List<VoteEntity> votes = voteService.retreive(promiseUid, userId);

            ResponseDTO response = ResponseDTO.<VoteEntity>builder().data(votes).status("succeed").build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
