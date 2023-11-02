package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.ResponseDTO;
import com.example.tomeettome.DTO.TestDTO;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    // TEST용 API
    @PostMapping
    public ResponseEntity<?> test(@RequestBody TestDTO dto) {
        log.info("server access!");
        String msg = dto.getOs() + ", Welcome to TMTM Server. What the HELL!";
        dto.setMsg(msg);

        ResponseDTO<TestDTO> response = ResponseDTO.<TestDTO>builder().data(Collections.singletonList(dto)).status("success").build();

        return ResponseEntity.ok().body(response);
    }

    @PutMapping
    public ResponseEntity<?> caldavTest(@RequestBody String calendarString) throws ParserException, IOException, ParseException {

//        StringReader sin = new StringReader(calendarString);
//        CalendarBuilder builder = new CalendarBuilder();
//        Calendar calendar = builder.build(sin);
//
//        log.info(String.valueOf(calendar.getComponent("VEVENT").getProperty(Property.SUMMARY).getClass().getSimpleName()));
//
//        String a  = calendar.getComponent("VEVENT").getProperty(Property.DTSTAMP).getValue();
//        DateTime dateTime = new DateTime(a);
//        dateTime.
////        Object obj = calendar.getComponent("VEVENT").getProperty(Property.DTSTAMP);
////        log.info(obj.getClass().getName());
//
//        log.info("dtstamp = " + a );
//
////        for (Component component : calendar.getComponents()) {
//////            log.info("Component : " + component.getName());
////
////            for (Property property : component.getProperties()) {
//////                log.info("Property : " + property.getName() + " Value: " + property.getValue());
////                log.info(String.valueOf(component.getProperty(Property.DTSTAMP)));
////                log.info(String.valueOf(component.getProperty(Property.DTSTAMP).getClass().getSimpleName()));
////
////            }
////        }

        Calendar calendar2 = new Calendar();
        calendar2.getProperties().add(Version.VERSION_2_0);
        calendar2.getProperties().add(new DtStart(new DateTime()));

        String calendarString2 = calendar2.toString();
        return ResponseEntity.ok().body(calendarString2);
    }
}
