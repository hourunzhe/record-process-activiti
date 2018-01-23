package org.activiti.cmd;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.util.List;

/**
 * @author 分享牛 http://www.shareniu.com/
 */
public class JumpTaskCmd implements Command<Void> {
    protected String taskId;
    protected String target;

    public JumpTaskCmd(String taskId, String target) {
        this.taskId = taskId;
        this.target = target;
    }

    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        HistoricTaskInstanceEntityManager historicTaskInstanceEntityManager = commandContext.getHistoricTaskInstanceEntityManager();
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        IdentityLinkEntityManager identityLinkEntityManager = commandContext.getIdentityLinkEntityManager();
        TaskEntity taskEntity = taskEntityManager.findById(taskId);
        ExecutionEntity ee = executionEntityManager.findById(taskEntity.getExecutionId());
        Process process = ProcessDefinitionUtil.getProcess(ee.getProcessDefinitionId());
        List<FlowElement> flowElements = (List<FlowElement>) process.getFlowElements();
        flowElements.forEach(flowElement -> {
            if (target.equals(flowElement.getName())) {
                ee.setCurrentFlowElement(flowElement);
                ActivitiEngineAgenda agenda = commandContext.getAgenda();
                agenda.planContinueProcessInCompensation(ee);
                identityLinkEntityManager.deleteIdentityLink(taskEntity, "", "master", "candidate");
                taskEntityManager.delete(taskEntity);
            }
        });
        return null;
    }

}  