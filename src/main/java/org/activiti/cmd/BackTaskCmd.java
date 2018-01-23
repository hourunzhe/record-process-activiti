package org.activiti.cmd;

import lombok.Cleanup;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.*;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BackTaskCmd implements Command<Void> {
    protected String taskId;

    public BackTaskCmd(String taskId) {
        this.taskId = taskId;
    }

    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        HistoricActivityInstanceEntityManager historicActivityInstanceEntityManager = commandContext.getHistoricActivityInstanceEntityManager();
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        IdentityLinkEntityManager identityLinkEntityManager = commandContext.getIdentityLinkEntityManager();
        HistoricActivityInstanceQueryImpl query = new HistoricActivityInstanceQueryImpl();
        TaskEntity taskEntity = taskEntityManager.findById(taskId);
        query.processInstanceId(taskEntity.getProcessInstanceId()).orderByHistoricActivityInstanceStartTime().desc();
        List<HistoricActivityInstance> historicActivityInstances = historicActivityInstanceEntityManager.findHistoricActivityInstancesByQueryCriteria(query, null);
        String previousTaskKey = null;
        List<String> needDeleteActivitiIds = new ArrayList<>();
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            if (historicActivityInstance.getActivityType().equals("userTask") && !taskEntity.getName().equals(historicActivityInstance.getActivityName())) {
                previousTaskKey = historicActivityInstance.getActivityId();
                needDeleteActivitiIds.add("'" + previousTaskKey + "'");
                break;
            } else {
                needDeleteActivitiIds.add("'" + historicActivityInstance.getActivityId() + "'");
            }
        }
        try {
            @Cleanup Connection connection = commandContext.getDbSqlSession().getSqlSession().getConnection();
            @Cleanup Statement state = null;
            String sql = "delete from ACT_HI_ACTINST where PROC_INST_ID_=" + taskEntity.getProcessInstanceId() +
                    " and ACT_ID_ in (" + String.join(",", needDeleteActivitiIds) + ")";
            state = connection.createStatement();
            state.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ExecutionEntity ee = executionEntityManager.findById(taskEntity.getExecutionId());
        Process process = ProcessDefinitionUtil.getProcess(taskEntity.getProcessDefinitionId());
        FlowElement flowElement = process.getFlowElement(previousTaskKey, true);
        ee.setCurrentFlowElement(flowElement);
        ActivitiEngineAgenda agenda = commandContext.getAgenda();
        agenda.planContinueProcessInCompensation(ee);
        taskEntity.getIdentityLinks().forEach(identityLinkEntity ->
                identityLinkEntityManager.deleteIdentityLink(identityLinkEntity, true));
        taskEntityManager.delete(taskEntity);
        return null;
    }


}