package com.example.tomeettome.Service;

import com.example.tomeettome.Model.AppointmentBlockEntity;
import com.example.tomeettome.Model.PromiseEntity;
import com.example.tomeettome.Repository.AppointmentBlockRepository;
import com.example.tomeettome.Repository.PromiseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PromiseService {
    @Autowired PromiseRepository promiseRepository;
    @Autowired AppointmentBlockRepository appointmentBlockRepository;
    public PromiseEntity create(PromiseEntity promise) {
        return promiseRepository.save(promise);
    }

    public List<PromiseEntity> retrieve(String icsFileName) {
        return promiseRepository.findByIcsFileName(icsFileName);
    }

    public List<AppointmentBlockEntity> retrieveAppointmentBlocks(String promiseUid) {
        return appointmentBlockRepository.findByPromiseUidOrderByRateDesc(promiseUid);
    }
    public PromiseEntity findByPromiseUid(String promiseUid) {
        return promiseRepository.findById(promiseUid).get();
    }

    // 약속의 Status를 바꾸고
    // 약속의 Dtstart와 DtEnd를 수정해야 되고
    // 약속의 Location 수정, 약속의 참여자 수정
    public PromiseEntity confirm(PromiseEntity promise){
        Optional<PromiseEntity> entity = promiseRepository.findById(promise.getUid());

        entity.ifPresent( p -> {
            List<List<String>> attendance = null;
            try {
                attendance = findAttendanceByTime(promise.getDtStart());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            p.setStatus("CONFIRMED");
            p.setLocation(promise.getLocation() != null ? promise.getLocation() : p.getLocation());
            p.setDtStart(promise.getDtStart());
            p.setDtEnd(promise.getDtEnd());
            p.setAttendee(attendance.get(0).toString());
            p.setAbsentee(attendance.get(1).toString());
            promiseRepository.save(p);
        });

        return promiseRepository.findById(promise.getUid()).get();
    }

    public List<List<String>> findAttendanceByTime(LocalDateTime dtStart) throws IOException {
        List<List<String>> result = new ArrayList<>();
        AppointmentBlockEntity entity = appointmentBlockRepository.findByTimestamp(Timestamp.valueOf(dtStart));
        List<String> attendee = extractNames(entity.getAttendee());
        List<String> absentee = extractNames(entity.getAbsentee());

        result.add(attendee);
        result.add(absentee);
        return result;
    }

    private static List<String> extractNames(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> nodes = mapper.readValue(jsonString, new TypeReference<List<JsonNode>>() {});

        List<String> names = new ArrayList<>();
        for (JsonNode node : nodes) {
            names.add(node.get("name").asText());
        }

        return names;
    }

    public boolean isPromiseConfirmed(PromiseEntity promise) {
        Optional<PromiseEntity> promiseEntity = promiseRepository.findById(promise.getUid());
        if (promiseEntity.get().getStatus().equals("TENTATIVE")) {
            return true;
        }
        else return false;
    }

    public void deletePromise(String promiseUid) {
        promiseRepository.delete(promiseRepository.findById(promiseUid).get());
    }
}
