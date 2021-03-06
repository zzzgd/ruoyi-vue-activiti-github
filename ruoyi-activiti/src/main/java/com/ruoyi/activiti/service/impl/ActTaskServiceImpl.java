

package com.ruoyi.activiti.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.ruoyi.activiti.domain.ActWorkflowFormData;
import com.ruoyi.activiti.domain.dto.ActWorkflowFormDataDTO;
import com.ruoyi.activiti.domain.dto.ActTaskDTO;
import com.ruoyi.activiti.service.IActTaskService;
import com.ruoyi.activiti.service.IActWorkflowFormDataService;
import com.ruoyi.common.core.page.PageDomain;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.CreateTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.CreateTaskPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ruoyi.activiti.constant.ActivitiConst.PROCESS_KEY_CUSTOMER_COMPLAINT;

@Service
public class ActTaskServiceImpl implements IActTaskService {

  private Logger log = Logger.getLogger(ActTaskServiceImpl.class.getName());

  @Autowired
  private RepositoryService repositoryService;
  @Autowired
  private HistoryService historyService;
  @Autowired
  private TaskService taskService;
  @Autowired
  private TaskRuntime taskRuntime;
  @Autowired
  private RuntimeService runtimeService;
  @Autowired
  private IActWorkflowFormDataService actWorkflowFormDataService;


  @Override
  public Page<ActTaskDTO> selectProcessDefinitionList(PageDomain pageDomain) {
    Page<ActTaskDTO> list = new Page<ActTaskDTO>();
    org.activiti.api.runtime.shared.query.Page<Task> pageTasks = taskRuntime.tasks(Pageable.of((pageDomain.getPageNum() - 1) * pageDomain.getPageSize(), pageDomain.getPageSize()));
    List<Task> tasks = pageTasks.getContent();
    int totalItems = pageTasks.getTotalItems();
    list.setTotal(totalItems);
    if (totalItems != 0) {
      //????????????,?????????taskName??????processInstanceid
//      Set<String> processInstanceIdIds = tasks.parallelStream().map(t -> "@@zhihui".equals(t.getFormKey()) ? t.getName() : t.getProcessInstanceId()).collect(Collectors.toSet());
      Set<String> processInstanceIdIds = tasks.parallelStream().map(t -> t.getProcessInstanceId()).collect(Collectors.toSet());
      List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIdIds).list();
      List<ActTaskDTO> actTaskDTOS = tasks.stream()
//              .map(t -> new ActTaskDTO(t, processInstanceList.parallelStream().filter(pi -> ("@@zhihui".equals(t.getFormKey()) ? t.getName() : t.getProcessInstanceId()).equals(pi.getId())).findAny().get()))
              .map(t -> new ActTaskDTO(t, processInstanceList.parallelStream().filter(pi -> t.getProcessInstanceId().equals(pi.getId())).findAny().get()))
              .collect(Collectors.toList());
      list.addAll(actTaskDTOS);

    }
    return list;
  }

  @Override//a2310dcb-f991-11eb-9a47-00ff64da1685
  public List<String> formDataShow(String taskID) {
    log.info("??????id:" + taskID);
    Task task = taskRuntime.task(taskID);
/*  ------------------------------------------------------------------------------
            FormProperty_0ueitp2--__!!??????--__!!??????--__!!????????????--__!!?????????
            ?????????
            FormProperty_0lovri0--__!!string--__!!??????--__!!f--__!!??????!!__--??????
            FormProperty_1iu6onu--__!!int--__!!??????--__!!s

            ?????????????????????????????????FormProperty_????????????????????????ID
            ???????????????f??????????????????s????????????t?????????(?????????int???????????????int?????????string)
            ?????????????????????????????????????????????????????????????????????????????????
            */

    //??????!!!!!!!!:??????Key formKey?????????????????????id???????????????????????????????????????key??????????????????????????????????????????key???task.getFormKey()???????????????key
    UserTask userTask = (UserTask) repositoryService.getBpmnModel(task.getProcessDefinitionId())
            .getFlowElement(task.getFormKey());
    log.info("task: " + task.getId() + "|assignee:" + task.getAssignee() + "|formKey:" + task.getFormKey());
    if (userTask == null) {
      return null;
    }
    List<FormProperty> formProperties = userTask.getFormProperties();
    List<String> collect = formProperties.stream().map(fp -> fp.getId()).collect(Collectors.toList());
    return collect;
  }

  @Override
  @Transactional
  public int formDataSave(String taskID, List<ActWorkflowFormDataDTO> awfs) throws ParseException {
    log.info("??????id:" + taskID);

    Task task = taskRuntime.task(taskID);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();

    Boolean hasVariables = false;//??????????????????
    HashMap<String, Object> variables = new HashMap<String, Object>();
    //????????????????????????????????????????????????
    List<ActWorkflowFormData> acwfds = new ArrayList<>();
    int i = -1;
    if (!awfs.isEmpty()) {
      for (ActWorkflowFormDataDTO awf : awfs) {
        ActWorkflowFormData actWorkflowFormData = new ActWorkflowFormData(processInstance.getBusinessKey(), awf, task);
        acwfds.add(actWorkflowFormData);
        //??????????????????
        if (!"f".equals(awf.getControlIsParam())) {
          variables.put(awf.getControlId(), awf.getControlValue());
          hasVariables = true;
        }
      }//for??????

      if (processInstance.getProcessDefinitionKey().equals(PROCESS_KEY_CUSTOMER_COMPLAINT)) {
        hasVariables = true;
        for (ActWorkflowFormDataDTO awf : awfs) {
          if ("complaint_operation_manager_check".equals(awf.getControlId()) && "????????????".equals(awf.getControlLable())) {
            //0?????????, 1 ?????????. ????????????/????????????
            variables.put("complaint_operation_manager_check_flow", awf.getControlValue().equals("0") ? "1" : "2");
            continue;
          }
          if ("complaint_operation_manager_check".equals(awf.getControlId()) && "?????????".equals(awf.getControlLable())) {
            variables.put("complaint_operation_manager_handle_users", awf.getControlValue());
            continue;
          }
          if ("complaint_consumer_service_confirm".equals(awf.getControlId()) && "????????????".equals(awf.getControlLable())) {
            if (awf.getControlValue().equals("0")) {
              //??????
              variables.put("complaint_consumer_service_confirm_flow ", "1");
            } else {
              //?????????

              Map<String, Object> variableMap = getVariableByTaskId(taskID, false);
              //????????????
              if (variableMap.getOrDefault("complaint_operation_manager_check_flow", "").equals(1)) {
                //complaint_consumer_service_confirm_flow
                variables.put("complaint_consumer_service_confirm_flow ", "3");
              } else if (variableMap.getOrDefault("complaint_operation_manager_check_flow", "").equals(2)) {
                //????????????
                variables.put("complaint_consumer_service_confirm_flow ", "2");
              } else if (variableMap.getOrDefault("complaint_consumer_service_check_flow", "").equals(3)) {
                //??????????????? ${complaint_consumer_service_check_flow == '3'}
                variables.put("complaint_consumer_service_confirm_flow ", "4");
              }
            }
            continue;
          }
          if ("complaint_consumer_service_check".equals(awf.getControlId()) && "????????????".equals(awf.getControlLable())) {
            variables.put("complaint_consumer_service_check_flow", awf.getControlValue());
          }

        }
      }
    } else {
      ActWorkflowFormData actWorkflowFormData = new ActWorkflowFormData(processInstance.getBusinessKey(), null, task);
      acwfds.add(actWorkflowFormData);
    }

    i = actWorkflowFormDataService.insertActWorkflowFormDatas(acwfds);

    if (task.getAssignee() == null) {
      taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
    }
    if (hasVariables) {
      //?????????????????????
      log.info("variables: " + JSON.toJSONString(variables));
      taskService.setVariables(taskID, variables);
      //false ???????????? ??????complete??????,?????????????????????????????????
//      taskService.complete(taskID, variables);
    }
    taskService.complete(taskID);
    return i;
  }


  /**
   * ?????????????????????
   */
  @Override
  public void backProcess(String taskID) throws Exception {
    Task task = taskRuntime.task(taskID);
    String processInstanceId = task.getProcessInstanceId();
    // ?????????????????????????????????????????????
    List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstanceId)
            .orderByTaskCreateTime()
            .desc()
            .list();
    if (ObjectUtils.isEmpty(htiList) || htiList.size() < 2) {
      return;
    }
    // list????????????????????????????????????
    HistoricTaskInstance lastTask = htiList.get(1);
    // list??????????????????????????????
    HistoricTaskInstance curTask = htiList.get(0);
    // ???????????????executionId
    String curExecutionId = curTask.getExecutionId();
    // ???????????????taskId
    String lastTaskId = lastTask.getId();
    // ???????????????executionId
    String lastExecutionId = lastTask.getExecutionId();
    if (null == lastTaskId) {
      throw new Exception("LAST TASK IS NULL");
    }
    String processDefinitionId = lastTask.getProcessDefinitionId();
    BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
    String lastActivityId = null;
    List<HistoricActivityInstance> haiFinishedList = historyService.createHistoricActivityInstanceQuery()
            .executionId(lastExecutionId).finished().list();
    for (HistoricActivityInstance hai : haiFinishedList) {
      if (lastTaskId.equals(hai.getTaskId())) {
        // ??????ActivityId?????????HistoricActivityInstance????????????????????????
        lastActivityId = hai.getActivityId();
        break;
      }
    }
    // ???????????????????????????
    FlowNode lastFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(lastActivityId);
    String name = lastFlowNode.getName();
    System.out.println("?????????????????????:" + name);
    // ???????????????????????????
    Execution execution = runtimeService.createExecutionQuery().executionId(curExecutionId).singleResult();
    String curActivityId = execution.getActivityId();
    FlowNode curFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(curActivityId);
    //????????????????????????????????????
    List<SequenceFlow> oriSequenceFlows = new ArrayList<>(curFlowNode.getOutgoingFlows());
    //??????????????????
    curFlowNode.getOutgoingFlows().clear();
    //???????????????
    List<SequenceFlow> newSequenceFlowList = new ArrayList<>();
    SequenceFlow newSequenceFlow = new SequenceFlow();
    newSequenceFlow.setId("newSequenceFlowId");
    newSequenceFlow.setSourceFlowElement(curFlowNode);
    newSequenceFlow.setTargetFlowElement(lastFlowNode);
    newSequenceFlowList.add(newSequenceFlow);
    curFlowNode.setOutgoingFlows(newSequenceFlowList);
    // ????????????
    taskService.complete(task.getId());
    //???????????????
    curFlowNode.setOutgoingFlows(oriSequenceFlows);
    org.activiti.engine.task.Task nextTask = taskService
            .createTaskQuery().processInstanceId(processInstanceId).singleResult();
    // ???????????????
    if (nextTask != null) {
      taskService.setAssignee(nextTask.getId(), lastTask.getAssignee());
    }
  }


  /**
   * ????????????
   *
   * @param taskId ????????????ID
   * @param isEnd  true ??????????????????, false??????start??????
   */
  @Override
  public void endTask(String taskId, boolean isEnd) {
    //  ????????????
    org.activiti.engine.task.Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
    FlowNode targetFlowNode;
    if (isEnd) {
      List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
      targetFlowNode = endEventList.get(0);
    } else {
      List<StartEvent> startEvents = bpmnModel.getMainProcess().findFlowElementsOfType(StartEvent.class);
      targetFlowNode = startEvents.get(0);
    }
    FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

    //  ???????????????????????????????????????
    List originalSequenceFlowList = new ArrayList<>();
    originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
    //  ??????????????????
    currentFlowNode.getOutgoingFlows().clear();

    //  ???????????????
    SequenceFlow newSequenceFlow = new SequenceFlow();
    newSequenceFlow.setId("newSequenceFlowId");
    newSequenceFlow.setSourceFlowElement(currentFlowNode);
    newSequenceFlow.setTargetFlowElement(targetFlowNode);
    List newSequenceFlowList = new ArrayList<>();
    newSequenceFlowList.add(newSequenceFlow);
    //  ??????????????????????????????
    currentFlowNode.setOutgoingFlows(newSequenceFlowList);
    //  ??????????????????
    taskService.complete(task.getId());
    //?????????end,  ?????????????????????????????????????????????????????????
    currentFlowNode.setOutgoingFlows(originalSequenceFlowList);
  }


  public Map<String, Object> getVariableByTaskId(String taskId, boolean isLocal) {
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    if (isLocal) {
      variablesMap = taskService.getVariablesLocal(taskId);
    } else {
      variablesMap = taskService.getVariables(taskId);
    }
    return variablesMap;
  }
}
