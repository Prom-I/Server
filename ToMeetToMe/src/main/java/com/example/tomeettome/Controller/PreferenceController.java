package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.CaldavDTO;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Service.PreferenceService;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
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
    public ResponseEntity<String> create(
                                    @PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String component) throws ParserException, IOException, ParseException, URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.add("Content-Disposition", "attachment; filename=calendar.ics");

        CaldavDTO dto = new CaldavDTO(component);
        PreferenceEntity entity = dto.toPreferenceEntity(dto);
        List<String> preferredDays = List.of(dto.getValue(component, Property.EXPERIMENTAL_PREFIX + "PREFERREDDAYS").split(","));
        String startScope = dto.getValue(component, Property.EXPERIMENTAL_PREFIX + "STARTSCOPE");
        String endScope = dto.getValue(component, Property.EXPERIMENTAL_PREFIX + "ENDSCOPE");
        String duration = dto.getValue(component, Property.DURATION);

        entity.setOrganizerId("yourUserId1");

        CaldavDTO.Precondition precondition = new CaldavDTO.Precondition(preferredDays,startScope,endScope,duration);

        return ResponseEntity.ok().headers(headers).body(CaldavDTO.setValue(preferenceService.create(entity, icsFileName,precondition)));
    }
}


