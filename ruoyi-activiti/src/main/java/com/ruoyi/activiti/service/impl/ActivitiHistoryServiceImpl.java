package com.ruoyi.activiti.service.impl;

import com.ruoyi.activiti.domain.dto.ActivitiHighLineDTO;
import com.ruoyi.activiti.manage.ProcessHistoryManager;
import com.ruoyi.activiti.service.IActivitiHistoryService;
import com.ruoyi.activiti.service.image.ProcessTraceService;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ActivitiHistoryServiceImpl implements IActivitiHistoryService {

  @Autowired
  private HistoryService historyService;
  @Autowired
  private ProcessTraceService processTraceService;
  @Autowired
  private RepositoryService repositoryService;
  @Autowired
  private ProcessHistoryManager processHistoryManager;


  /**
   * @param instanceId
   * @return
   */
  @Override
  public ActivitiHighLineDTO gethighLine(String instanceId) {
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(instanceId).singleResult();
    //获取bpmnModel对象
    BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
    //高亮的点,已完成
    Set<String> highPoint = processHistoryManager.getFinishedActivityId(instanceId);
    //未完成的节点
    List<HistoricActivityInstance> listUnFinished = processHistoryManager.getUnfinishedActiviti(instanceId);

    List<String> highLine = processTraceService.getHighLine(bpmnModel, instanceId);
    Set<String> iDo = new HashSet<>(); //存放 高亮 我的办理节点
    //当前用户已完成的任务
    List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery()
//                    .taskAssignee(SecurityUtils.getUsername())
            .finished()
            .processInstanceId(instanceId).list();

    taskInstanceList.forEach(a -> iDo.add(a.getTaskDefinitionKey()));

    //待办高亮节点
    Set<String> waitingToDo = new HashSet<>();
    listUnFinished.forEach(s -> waitingToDo.add(s.getActivityId()));

    ActivitiHighLineDTO activitiHighLineDTO = new ActivitiHighLineDTO();
    activitiHighLineDTO.setHighPoint(highPoint);
    activitiHighLineDTO.setHighLine(new HashSet<>(highLine));
    activitiHighLineDTO.setWaitingToDo(waitingToDo);
    activitiHighLineDTO.setiDo(iDo);

    return activitiHighLineDTO;
  }


  public boolean isFinished(String processInstanceId) {
    return historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(processInstanceId).count() > 0;
  }


}
