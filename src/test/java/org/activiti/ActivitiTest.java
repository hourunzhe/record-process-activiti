package org.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiTest {

    private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /**
     * &#x90e8;&#x7f72;&#x6d41;&#x7a0b;&#x5b9a;&#x4e49;
     */
    @Test
    public void deploy() {
        Deployment deployment = processEngine.getRepositoryService() // 获取部署相关Service
                .createDeployment() // 创建部署
                .addClasspathResource("processes/Record_Process.bpmn20.xml") // 加载资源文件
                .addClasspathResource("processes/start.form")
                .name("流程部署测试") // 流程名称
                .deploy(); // 部署
    }

    /**
     * 启动流程实例
     */
    @Test
    public void start() {
        ProcessInstance pi = processEngine.getRuntimeService() // 运行时Service
                .startProcessInstanceByKey("recordProcess"); // 流程定义表的KEY字段值
        System.out.println("流程实例ID:" + pi.getId());
        System.out.println("流程定义ID:" + pi.getProcessDefinitionId());
    }


    /**
     * 查看任务
     */
    @Test
    public void findTask() {
        List<Task> taskList = processEngine.getTaskService() // 任务相关Service
                .createTaskQuery() // 创建任务查询
                .processInstanceId("65001")
                .list();
        for (Task task : taskList) {
            System.out.println("任务ID:" + task.getId());
            System.out.println("任务名称:" + task.getName());
            System.out.println("任务创建时间:" + task.getCreateTime());
            System.out.println("任务委派人:" + task.getAssignee());
            System.out.println("流程实例ID:" + task.getProcessInstanceId());
        }
    }


    /**
     * 完成任务
     */
    @Test
    public void completeTask() {
        processEngine.getTaskService() // 任务相关Service
                .complete("65009");
    }


    @Test
    public void formTest() {
        Map<String, String> formProperties = new HashMap<String, String>();
        formProperties.put("applyUserId","");
        formProperties.put("leaveType","");
        formProperties.put("startTime","");
        formProperties.put("endTime","");
        formProperties.put("reason","");
        //启动流程-何静媛-2015年5月24日--processDefinitionId,
        ProcessInstance processInstance = processEngine.getFormService().submitStartFormData("recordProcess:7:32505",
                formProperties);

        System.out.println(processInstance.getId());
    }

    @Test
    public void formTest2() {
        Object renderedTaskForm = processEngine.getFormService().getRenderedTaskForm("37515");
        System.out.println(renderedTaskForm);
    }

}