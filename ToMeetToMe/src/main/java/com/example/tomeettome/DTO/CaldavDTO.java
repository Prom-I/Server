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

     END:VEVENT
     END:VCALENDAR
     */

    /**
     private String originKey;
     private String calendarOriginKey;
     private String categoryOriginKey;
     private String summary; // 일정의 제목
     private String description; // 일정의 상세설명
     private String dtStart; // 시작 시간
     private String dtEnd; // 종료 시간
     private String location;
     private String rRule;
     private String status;
     private String allDay;
    */
    public static ScheduleEntity toEntity(final CaldavDTO dto) throws ParserException, IOException, ParseException {
        StringReader sin = new StringReader(dto.calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);

        String summary  = calendar.getComponent("VEVENT").getProperty(Property.SUMMARY).getValue();
        String location = calendar.getComponent("VEVENT").getProperty(Property.LOCATION).getValue();
        String description  = calendar.getComponent("VEVENT").getProperty(Property.DESCRIPTION).getValue();

        try {
            DateTime dtStart  = new DateTime(calendar.getComponent("VEVENT").getProperty(Property.DTSTART).getValue());
            DateTime dtEnd  = new DateTime(calendar.getComponent("VEVENT").getProperty(Property.DTEND).getValue());

            String dtStartString = dtStart.toString();

            // 시간 부분만 추출
            String startTime = dtStartString.substring(9, 15);

            String allDay = "false";

            // 하루 동안 지속되고, 00시부터 00시까지인지 확인
            if ("000000".equals(startTime)) {
                // 두 DateTime 객체의 차이를 계산
                long timeDifference = dtEnd.getTime() - dtStart.getTime();
                long oneDayInMillis = 24 * 60 * 60 * 1000; // 1일을 밀리초로

                if (timeDifference == oneDayInMillis) {
                    System.out.println("정확한 하루 차이입니다.");
                    allDay = "true";

                } else {
                    System.out.println("하루 차이가 아닙니다.");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
