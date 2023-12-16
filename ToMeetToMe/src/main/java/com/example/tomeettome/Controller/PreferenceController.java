package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.CaldavDTO;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Service.PreferenceService;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/preference")
public class PreferenceController {

    @Autowired PreferenceService preferenceService;
    /**
     *
     * @param userId Preference의 Organizer
     * @param icsFileName Team Calender의 ics File Name
     * @param component 실제 ics File
     * @return
     * @throws ParserException
     * @throws IOException
     */
//    @AuthenticationPrincipal String userId,
    @PutMapping("/create/{icsFileName}")
    public ResponseEntity<?> create(
                                    @PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String component) throws ParserException, IOException {
        CaldavDTO dto = new CaldavDTO(component);
        PreferenceEntity entity = dto.toPreferenceEntity(dto);
        List<String> preferredDays = List.of(dto.getValue(component, Property.EXPERIMENTAL_PREFIX + "PREFERREDDAYS").split(","));
        entity.setOrganizerId("yourUserId1");

        entity = preferenceService.create(entity, preferredDays, icsFileName);

        return ResponseEntity.ok().body(null);
    }
}
