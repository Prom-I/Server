package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.ScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

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
    public static ScheduleEntity toEntity(final CaldavDTO dto) throws ParserException, IOException {
        StringReader sin = new StringReader(dto.calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);

        return ScheduleEntity.builder()
                .summary(calendar.getComponent("VEVENT").getProperty(Property.SUMMARY).getValue())
                .description(calendar.getComponent("VEVENT").getProperty(Property.DESCRIPTION).getValue())
                .dtStart(calendar.getComponent("VEVENT").getProperty(Property.DTSTART).getValue())
                .dtEnd(calendar.getComponent("VEVENT").getProperty(Property.DTEND).getValue())
                .location(calendar.getComponent("VEVENT").getProperty(Property.LOCATION).getValue())
                .rRule(calendar.getComponent("VEVENT").getProperty(Property.RRULE).getValue())
                .status(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX +"STATUS").getValue())
                .allDay(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "ALLDAY").getValue())
                .build();
    }

    public String getValue(String calendarString, String property) throws ParserException, IOException {
        StringReader sin = new StringReader(calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);
        String value = calendar.getComponent("VEVENT").getProperty(Property.CATEGORIES).getValue();

        return value;
    }


}
