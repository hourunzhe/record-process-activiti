package org.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

public class ParallelGatewayTest {

    /**
     * 获取默认流程引擎实例，会自动读取activiti.cfg.xml文件
     */
    private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /**
     * &#x90e8;&#x7f72;&#x6d41;&#x7a0b;&#x5b9a;&#x4e49;
     */
    @Test
    public void deploy() {
        Deployment deployment = processEngine.getRepositoryService() // 获取部署相关Service
                .createDeployment() // 创建部署
                .addClasspathResource("diagrams/gatewayParallel.bpmn") // 加载资源文件
                .addClasspathResource("diagrams/gatewayParallel.png") // 加载资源文件
                .name("学生请假流程4") // 流程名称
                .deploy(); // 部署
        System.out.println("流程部署ID:" + deployment.getId());
        System.out.println("流程部署Name:" + deployment.getName());
    }

    /**
     * 启动流程实例
     */
    @Test
    public void start() {
        ProcessInstance pi = processEngine.getRuntimeService() // 运行时Service
                .startProcessInstanceByKey("parallelGateway"); // 流程定义表的KEY字段值
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


}