package com.example.tomeettome.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "AppointmentBlock")
public class AppointmentBlockEntity {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    private String uid;

    private Timestamp timestamp;

    private String promiseUid;

    private int rate;

    @Column(columnDefinition = "JSON")
    private String absentee;

    @Column(columnDefinition = "JSON")
    private String attendee;


}
