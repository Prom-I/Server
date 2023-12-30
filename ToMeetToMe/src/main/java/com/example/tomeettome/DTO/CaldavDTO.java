package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CaldavDTO {
    private String calendarString;

    /**
     BEGIN:VCALENDAR
     PRODID:-//Example Corp.//CalDAV Client//EN
     VERSION:2.0
     BEGIN:VEVENT
     UID:1@example.com
     SUMMARY:One-off Meeting
     DTSTAMP:20041210T183904Z
     DTSTART:20041207T120000Z
     DTEND:20041207T130000Z
     RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4
     CATEGORIES:회의
     STATUS:TENTATIVE // 그룹일정인 경우, 확정/취소/미확정
     X-ALLDAY:TRUE
     X-STATUS:IN-PROCESS
     END:VEVENT
     END:VCALENDAR
     */

    /**
     TENTATIVE: 이벤트가 아직 최종 확정되지 않았음을 나타냅니다.
     CONFIRMED: 이벤트가 최종적으로 확정되었음을 나타냅니다.
     CANCELLED: 이벤트가 취소되었음을 나타냅니다.
    */

    /**
     statvalue-todo  =
     / "NEEDS-ACTION" ;Indicates to-do needs action.
     / "COMPLETED"    ;Indicates to-do completed.
     / "IN-PROCESS"   ;Indicates to-do in process of.
     / "CANCELLED"    ;Indicates to-do was cancelled.
     */
    public static ScheduleEntity toScheduleEntity(final CaldavDTO dto) throws ParserException, IOException {
        StringReader sin = new StringReader(dto.calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);

        return ScheduleEntity.builder()
                .uid(calendar.getComponent("VEVENT").getProperty(Property.UID).getValue())
                .summary(calendar.getComponent("VEVENT").getProperty(Property.SUMMARY).getValue())
                .description(calendar.getComponent("VEVENT").getProperty(Property.DESCRIPTION).getValue()!=null ?
                        calendar.getComponent("VEVENT").getProperty(Property.DESCRIPTION).getValue() : "")
                .dtStart(Timestamp.valueOf(LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTSTART).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))))
                .dtEnd(Timestamp.valueOf(LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTEND).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))))
                .location(calendar.getComponent("VEVENT").getProperty(Property.LOCATION).getValue()!=null ?
                        calendar.getComponent("VEVENT").getProperty(Property.LOCATION).getValue() : "")
                .rRule(calendar.getComponent("VEVENT").getProperty(Property.RRULE).getValue()!= null ?
                        calendar.getComponent("VEVENT").getProperty(Property.RRULE).getValue() : "")
                .status(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "STATUS").getValue())
                .allDay(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "ALLDAY").getValue())
                .build();
    }

    public static PreferenceEntity toPreferenceEntity(final CaldavDTO dto) throws ParserException, IOException {
        StringReader sin = new StringReader(dto.calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);

        return PreferenceEntity.builder()
                .uid(calendar.getComponent("VEVENT").getProperty(Property.UID).getValue())
                .dtStart(LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTSTART).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))
                .dtEnd(LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTEND).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))

//                .duration(calendar.getComponent("VEVENT").getProperty(Property.DURATION).getValue())
//                .startScope(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "STARTSCOPE").getValue())
//                .endScope(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "ENDSCOPE").getValue())

                .status(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "STATUS").getValue())
//                .allDay(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "ALLDAY").getValue())
                .build();
    }

    public String getValue(String calendarString, String property) throws ParserException, IOException {
        StringReader sin = new StringReader(calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);
        String value = calendar.getComponent("VEVENT").getProperty(property).getValue();

        return value;
    }


    public static String setValue(List<PreferenceEntity> pList) throws ParseException, URISyntaxException, IOException {

        Calendar calendar = new Calendar();

        for (PreferenceEntity pf : pList) {
            VEvent vEvent = new VEvent();
            vEvent.getProperties().add(new Uid(pf.getUid()));
            vEvent.getProperties().add(new DtStart(new DateTime(Date.from(pf.getDtStart().atZone(ZoneId.systemDefault()).toInstant()))));
            vEvent.getProperties().add(new DtEnd(new DateTime(Date.from(pf.getDtEnd().atZone(ZoneId.systemDefault()).toInstant()))));
            vEvent.getProperties().add(new Organizer(pf.getOrganizerId()));
            vEvent.getProperties().add(new Status(pf.getStatus()));
            vEvent.getProperties().add(new XProperty(Property.EXPERIMENTAL_PREFIX + "LIKES", String.valueOf(pf.getLikes())));
            calendar.getComponents().add(vEvent);
        }
        return calendar.toString();
    }

    public static class Precondition {
        List<String> preferredDays;
        String startScope;
        String endScope;
        String duration;

    public Precondition(List<String> preferredDays, String startScope, String endScope, String duration) {
        this.preferredDays = preferredDays;
        this.startScope = startScope;
        this.endScope = endScope;
        this.duration = duration;
    }

    public List<String> getPreferredDays() {
        return preferredDays;
    }

    public String getStartScope() {
        return startScope;
    }

    public String getEndScope() {
        return endScope;
    }

    public String getDuration() {
        return duration;
    }
}
}


