package org.activiti.rest;


import lombok.Cleanup;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.cmd.BackTaskCmd;
import org.activiti.cmd.JumpTaskCmd;
import org.activiti.diagram.CoustomProcessDiagramGenerator;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;


@Controller
@RequestMapping(value = "/activiti")
public class ActSprBootController {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    HistoryService historyService;

    @Autowired
    TaskService taskService;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    ManagementService managementService;


    @ResponseBody
    @RequestMapping(value = "/jump/{taskId}", method = RequestMethod.GET)
    public void jump(@PathVariable("taskId") String taskId) {
        managementService.executeCommand(new JumpTaskCmd(taskId, "纸质材料审查"));
    }

    @ResponseBody
    @RequestMapping(value = "/back/{taskId}", method = RequestMethod.GET)
    public void back(@PathVariable("taskId") String taskId) {
        managementService.executeCommand(new BackTaskCmd(taskId));
    }

    @ResponseBody
    @RequestMapping(value = "/bpmn", method = RequestMethod.GET)
    public void getBpmnModel(HttpServletResponse response) throws IOException {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("recordProcess").list();
        int bytesWritten = 0;
        int byteCount = 0;
        byte[] bytes = new byte[1024];
        @Cleanup OutputStream outputStream = response.getOutputStream();
        response.setContentType("image/png");
        response.setHeader("Content-disposition",
                "inline;filename=" + URLEncoder.encode(new Date().getTime() + ".png", "utf-8"));
        String diagramResourceName = processDefinitions.get(0).getDiagramResourceName();
        @Cleanup InputStream imageStream = repositoryService
                .getResourceAsStream(processDefinitions.get(0).getDeploymentId(), diagramResourceName);
        while ((byteCount = imageStream.read(bytes)) != -1) {
            outputStream.write(bytes, bytesWritten, byteCount);
            outputStream.flush();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/bpmn/v2/{taskId}", method = RequestMethod.GET)
    public void getPng(@PathVariable("taskId") String taskId, HttpServletResponse response) throws IOException {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        // 经过的节点
        List<String> activeActivityIds = new ArrayList<>();
        List<String> finishedActiveActivityIds = new ArrayList<>();
        // 已执行完的任务节点
        List<HistoricActivityInstance> finishedInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(task.getProcessInstanceId()).finished().list();
        for (HistoricActivityInstance hai : finishedInstances) {
            finishedActiveActivityIds.add(hai.getActivityId());
        }
        // 已完成的节点+当前节点
        activeActivityIds.addAll(finishedActiveActivityIds);
        activeActivityIds.addAll(runtimeService.getActiveActivityIds(task.getProcessInstanceId()));
        // 经过的流
        Process process = bpmnModel.getProcess(null);
        Map<String, FlowElement> flowElements = process.getFlowElementMap();
        List<String> highLightedFlows = new ArrayList<>();
        getHighLightedFlows(flowElements, highLightedFlows, activeActivityIds);

        CoustomProcessDiagramGenerator pdg = new CoustomProcessDiagramGenerator();
        @Cleanup InputStream imageStream = pdg.generateDiagram(bpmnModel, "PNG", activeActivityIds, highLightedFlows);
        int bytesWritten = 0;
        int byteCount = 0;
        byte[] bytes = new byte[1024];
        @Cleanup OutputStream outputStream = response.getOutputStream();
        response.setContentType("image/png");
        response.setHeader("Content-disposition",
                "inline;filename=" + URLEncoder.encode(new Date().getTime() + ".png", "utf-8"));
        while ((byteCount = imageStream.read(bytes)) != -1) {
            outputStream.write(bytes, bytesWritten, byteCount);
            outputStream.flush();
        }
    }

    private void getHighLightedFlows(Map<String, FlowElement> flowElements, List<String> highLightedFlows, List<String> activeActivityIds) {
        flowElements.forEach((s, flowElement) -> {
            if (flowElement instanceof SequenceFlow
                    && activeActivityIds.contains(((SequenceFlow) flowElement).getSourceRef())
                    && activeActivityIds.contains(((SequenceFlow) flowElement).getTargetRef())) {
                highLightedFlows.add(s);
            }
        });
    }


}
