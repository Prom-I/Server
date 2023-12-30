package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.VoteDTO;
import com.example.tomeettome.Model.VoteEntity;
import com.example.tomeettome.Service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/vote")
public class VoteController {
    @Autowired VoteService voteService;

    @PutMapping("/like")
    public ResponseEntity<?> like(@AuthenticationPrincipal String userId,
                                  @RequestBody VoteDTO dto) {
        voteService.like(userId, VoteDTO.toEntity(dto));
        return null;
    }

    @PutMapping("/dislike")
    public ResponseEntity<?> dislike(@AuthenticationPrincipal String userId,
                                     @RequestBody VoteDTO dto) {
        voteService.dislike(userId, VoteDTO.toEntity(dto));
        return null;
    }

}
