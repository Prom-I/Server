package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.PromiseEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.PartStatFactory;
import net.fortuna.ical4j.model.property.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
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
                .description(calendar.getComponent("VEVENT").getProperty(Property.DESCRIPTION)!= null ?
                        calendar.getComponent("VEVENT").getProperty(Property.DESCRIPTION).getValue() : "")
                .dtStart(Timestamp.valueOf(LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTSTART).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))))
                .dtEnd(Timestamp.valueOf(LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTEND).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))))
                .location(calendar.getComponent("VEVENT").getProperty(Property.LOCATION) != null ?
                        calendar.getComponent("VEVENT").getProperty(Property.LOCATION).getValue() : "")
                .rRule(calendar.getComponent("VEVENT").getProperty(Property.RRULE) != null ?
                        calendar.getComponent("VEVENT").getProperty(Property.RRULE).getValue() : "")
                .status(calendar.getComponent("VEVENT").getProperty(Property.STATUS).getValue())
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
                .dtEnd( LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTEND).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))
                .location(calendar.getComponent("VEVENT").getProperty(Property.LOCATION) != null ?
                        calendar.getComponent("VEVENT").getProperty(Property.LOCATION).getValue() : "")
//                .duration(calendar.getComponent("VEVENT").getProperty(Property.DURATION).getValue())
//                .startScope(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "STARTSCOPE").getValue())
//                .endScope(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "ENDSCOPE").getValue())

//                .status(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "STATUS").getValue())
//                .allDay(calendar.getComponent("VEVENT").getProperty(Property.EXPERIMENTAL_PREFIX + "ALLDAY").getValue())
                .build();
    }

    public static PromiseEntity toPromiseEntity(final CaldavDTO dto) throws ParserException, IOException {
        StringReader sin = new StringReader(dto.calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);

        return PromiseEntity.builder()
                .uid(calendar.getComponent("VEVENT").getProperty(Property.UID).getValue())
                .dtStart(LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTSTART).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))
                .dtEnd( LocalDateTime.parse(calendar.getComponent("VEVENT").getProperty(Property.DTEND).getValue(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))
                .build();
    }



    public String getValue(String calendarString, String property) throws ParserException, IOException {
        StringReader sin = new StringReader(calendarString);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(sin);
        String value = calendar.getComponent("VEVENT").getProperty(property).getValue();

        return value;
    }


    public static String setPreferenceValue(List<PreferenceEntity> pList, PromiseEntity promise) throws ParseException, URISyntaxException, IOException {

        Calendar calendar = new Calendar();

        for (PreferenceEntity pf : pList) {
            VEvent vEvent = new VEvent();
            vEvent.getProperties().add(new Uid(pf.getUid()));
            vEvent.getProperties().add(new Summary(promise.getSummary())); // 제목
            vEvent.getProperties().add(new DtStart(new DateTime(Date.from(pf.getDtStart().atZone(ZoneId.systemDefault()).toInstant()))));
            vEvent.getProperties().add(new DtEnd(new DateTime(Date.from(pf.getDtEnd().atZone(ZoneId.systemDefault()).toInstant()))));
            vEvent.getProperties().add(new Organizer(promise.getOrganizerId())); // 약속 생성자
            vEvent.getProperties().add(new Status(promise.getStatus())); // 약속 상태
            vEvent.getProperties().add(new XProperty(Property.EXPERIMENTAL_PREFIX + "LIKES", String.valueOf(pf.getLikes())));
            vEvent.getProperties().add(new XProperty(Property.EXPERIMENTAL_PREFIX + "PROMISEUID", promise.getUid())); // 약속 UID
            calendar.getComponents().add(vEvent);
        }
        return calendar.toString();
    }

    public static String setPromiseValue(List<PromiseEntity> pList) throws URISyntaxException {
        Calendar calendar = new Calendar();

        for (PromiseEntity p : pList) {
            VEvent vEvent = new VEvent();
            vEvent.getProperties().add(new Uid(p.getUid()));
            vEvent.getProperties().add(new Summary(p.getSummary()));
            vEvent.getProperties().add(new DtStart(new DateTime(Date.from(p.getDtStart().atZone(ZoneId.systemDefault()).toInstant()))));
            vEvent.getProperties().add(new DtEnd(new DateTime(Date.from(p.getDtEnd().atZone(ZoneId.systemDefault()).toInstant()))));
            vEvent.getProperties().add(new Organizer(p.getOrganizerId()));
            vEvent.getProperties().add(new Status(p.getStatus()));
            vEvent.getProperties().add(new XProperty(Property.EXPERIMENTAL_PREFIX + "DDAYS", String.valueOf(calculateDday(p.getDtStart()))));
            if (p.getStatus().equals("CONFIRMED")) { // 확정된 약속
                //참석자랑 불참자 세팅
                for(String attendee : stringToList(p.getAttendee())){
                    vEvent.getProperties().add((Property) new Attendee().withParameter(new Cn(attendee)).withParameter(new PartStat("ACCEPTED")));
                }
                for(String absentee : stringToList(p.getAbsentee())){
                    vEvent.getProperties().add((Property) new Attendee().withParameter(new Cn(absentee)).withParameter(new PartStat("DECLINED")));
                }

            }
            calendar.getComponents().add(vEvent);
        }
        return calendar.toString();
    }



    // [{"uid": "yourUserId2", "name": "YourUserName2#2"},
    // {"uid": "yourUserId3", "name": "YourUserName3#3"},
    // {"uid": "yourUserId4", "name": "YourUserName4#4"},
    // {"uid": "yourUserId5", "name": "YourUserName5#5"},
    // {"uid": "yourUserId6", "name": "YourUserName6#6"},
    // {"uid": "yourUserId7", "name": "YourUserName7#7"},
    // {"uid": "yourUserId8", "name": "YourUserName8#8"},
    // {"uid": "yourUserId9", "name": "YourUserName9#9"},
    // {"uid": "yourUserId10", "name": "YourUserName10#10"},
    // {"uid": "yourUserId11", "name": "YourUserName11#11"},
    // {"uid": "yourUserId12", "name": "YourUserName12#12"},
    // {"uid": "yourUserId13", "name": "YourUserName13#13"},
    // {"uid": "yourUserId14", "name": "YourUserName14#14"},
    // {"uid": "yourUserId15", "name": "YourUserName15#15"}]
    public static String setScheduleValue(List<ScheduleEntity> sList) throws ParseException {
        Calendar calendar = new Calendar();

        if (sList != null) {
            for (ScheduleEntity s : sList) {
                VEvent vEvent = new VEvent();
                vEvent.getProperties().add(new Uid(s.getUid()));
                vEvent.getProperties().add(new Summary(s.getSummary()));
                vEvent.getProperties().add(new Description(s.getDescription() != null ? s.getDescription() : ""));
                vEvent.getProperties().add(new DtStart(s.getDtStart().toInstant().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))));
                vEvent.getProperties().add(new DtStart(s.getDtEnd().toInstant().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))));
                vEvent.getProperties().add(new Location(s.getLocation() != null ? s.getDescription() : ""));
                vEvent.getProperties().add(new Status(s.getStatus()));
                vEvent.getProperties().add(new XProperty(Property.EXPERIMENTAL_PREFIX + "ICSFILENAME", s.getIcsFileName()));
                vEvent.getProperties().add(new XProperty(Property.EXPERIMENTAL_PREFIX + "CATEGORYUID", s.getCategoryUid()));
                vEvent.getProperties().add(new XProperty(Property.EXPERIMENTAL_PREFIX + "ALLDAY", s.getAllDay()));

                calendar.getComponents().add(vEvent);
            }
            return calendar.toString();
        }
        else return null;
    }

    public static class Precondition {
        List<String> preferredDays;
        String startScope;
        String endScope;
        String duration;
        String promiseUid;

    public Precondition(List<String> preferredDays, String startScope, String endScope, String duration, String promiseUid) {
        this.preferredDays = preferredDays;
        this.startScope = startScope;
        this.endScope = endScope;
        this.duration = duration;
        this.promiseUid = promiseUid;
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

    public String getPromiseUid() {
        return promiseUid;
    }
    }
    public static List<String> stringToList(String input) {
        if (input == null || input.equals("[]")) {
            return Collections.emptyList();
        }

        // 대괄호 제거 및 쉼표로 분리
        input = input.substring(1, input.length() - 1); // 대괄호 제거
        String[] items = input.split(",\\s*");

        return Arrays.asList(items);
    }

    public static long calculateDday(LocalDateTime dDay) {
        LocalDateTime today = LocalDateTime.now();

        // 오늘부터 D-Day까지 몇 일 남았는지 계산
        return ChronoUnit.DAYS.between(today, dDay) + 1;
    }

}


