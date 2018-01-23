package org.activiti.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "record_state")
    private RecordState recordState;
}
