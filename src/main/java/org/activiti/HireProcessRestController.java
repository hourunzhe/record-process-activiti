package org.activiti;


import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.application.ContentTypeResolver;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.service.api.runtime.task.TaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class HireProcessRestController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    protected ContentTypeResolver contentTypeResolver;


    @Autowired
    protected RestResponseFactory restResponseFactory;

    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/start-hire-process", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void startHireProcess() {
        runtimeService.startProcessInstanceByKey("hireProcessWithJpa");
    }


    @RequestMapping(value = "/form-step", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProcessInstanceResponse> startFormStep() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("formstep");
        return ResponseEntity.ok().body(restResponseFactory.createProcessInstanceResponse(processInstance));
    }


    @RequestMapping(value = "/form-step/{instanceId}/task", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<TaskResponse> getTaskByInstanceId(@PathVariable(value = "instanceId") String instanceId) {
        Task task = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        return ResponseEntity.ok().body(restResponseFactory.createTaskResponse(task));
    }


    @RequestMapping(value = "/form-step/{instanceId}/task/{taskId}/complete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProcessInstanceResponse> getTaskByInstanceIdComplete(@PathVariable(value = "instanceId") String instanceId,
                                                                               @PathVariable(value = "taskId") String taskId,
                                                                               @RequestBody Map<String, String> data) {
        data.forEach((k, v) -> taskService.setVariable(taskId, k, v));
        taskService.complete(taskId);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (processInstance == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok().body(restResponseFactory.createProcessInstanceResponse(processInstance));
    }



}