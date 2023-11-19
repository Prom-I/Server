package com.example.tomeettome.Controller;

import com.example.tomeettome.Model.ScheduleEntity;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController {


    @PutMapping("/create")
    public ResponseEntity<?> caldavTest(@RequestBody String calendarString) throws ParserException, IOException, ParseException {

        StringReader sin = new StringReader(calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);

        String a  = calendar.getComponent("VEVENT").getProperty(Property.DTSTAMP).getValue();

        ScheduleEntity schedule = ScheduleEntity.builder().build();
    }
}