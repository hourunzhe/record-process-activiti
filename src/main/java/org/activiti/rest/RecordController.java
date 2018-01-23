package org.activiti.rest;

import org.activiti.model.Record;
import org.activiti.model.RecordState;
import org.activiti.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping(value = "/records")
public class RecordController {
    @Autowired
    private RecordRepository recordRepository;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Record> save(@RequestBody Record record) {
        record.setRecordState(RecordState.INREVIEW);
        return ResponseEntity.ok().body(recordRepository.save(record));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Record>> getAll() {
        return ResponseEntity.ok().body(recordRepository.findAll());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Record> getById(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok().body(recordRepository.findOne(id));
    }

    @Transactional
    @RequestMapping(value = "/{id}/reject", method = RequestMethod.PUT)
    public ResponseEntity<Record> rejectById(@PathVariable(value = "id") Long id) {
        Record record = recordRepository.findOne(id);
        record.setRecordState(RecordState.REJECT);
        return ResponseEntity.ok().body(recordRepository.findOne(id));
    }

    @Transactional
    @RequestMapping(value = "/{id}/pass", method = RequestMethod.PUT)
    public ResponseEntity<Record> passById(@PathVariable(value = "id") Long id) {
        Record record = recordRepository.findOne(id);
        record.setRecordState(RecordState.PASS);
        return ResponseEntity.ok().body(recordRepository.findOne(id));
    }
}
