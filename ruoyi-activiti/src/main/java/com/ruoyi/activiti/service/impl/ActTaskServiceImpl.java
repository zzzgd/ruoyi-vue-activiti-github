

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
      //如果知会,取得是taskName作为processInstanceid
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
    log.info("任务id:" + taskID);
    Task task = taskRuntime.task(taskID);
/*  ------------------------------------------------------------------------------
            FormProperty_0ueitp2--__!!类型--__!!名称--__!!是否参数--__!!默认值
            例子：
            FormProperty_0lovri0--__!!string--__!!姓名--__!!f--__!!同意!!__--驳回
            FormProperty_1iu6onu--__!!int--__!!年龄--__!!s

            默认值：无、字符常量、FormProperty_开头定义过的控件ID
            是否参数：f为不是参数，s是字符，t是时间(不需要int，因为这里int等价于string)
            注：类型是可以获取到的，但是为了统一配置原则，都配置到
            */

    //注意!!!!!!!!:表单Key formKey必须要和节点的id一模一样，因为参数需要任务key，但是无法获取，只能获取表单key“task.getFormKey()”当做任务key
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
    log.info("任务id:" + taskID);

    Task task = taskRuntime.task(taskID);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();

    Boolean hasVariables = false;//没有任何参数
    HashMap<String, Object> variables = new HashMap<String, Object>();
    //前端传来的字符串，拆分成每个控件
    List<ActWorkflowFormData> acwfds = new ArrayList<>();
    int i = -1;
    if (!awfs.isEmpty()) {
      for (ActWorkflowFormDataDTO awf : awfs) {
        ActWorkflowFormData actWorkflowFormData = new ActWorkflowFormData(processInstance.getBusinessKey(), awf, task);
        acwfds.add(actWorkflowFormData);
        //构建参数集合
        if (!"f".equals(awf.getControlIsParam())) {
          variables.put(awf.getControlId(), awf.getControlValue());
          hasVariables = true;
        }
      }//for结束

      if (processInstance.getProcessDefinitionKey().equals(PROCESS_KEY_CUSTOMER_COMPLAINT)) {
        hasVariables = true;
        for (ActWorkflowFormDataDTO awf : awfs) {
          if ("complaint_operation_manager_check".equals(awf.getControlId()) && "是否指派".equals(awf.getControlLable())) {
            //0表示是, 1 表示否. 指派他人/自行处理
            variables.put("complaint_operation_manager_check_flow", awf.getControlValue().equals("0") ? "1" : "2");
            continue;
          }
          if ("complaint_operation_manager_check".equals(awf.getControlId()) && "指派人".equals(awf.getControlLable())) {
            variables.put("complaint_operation_manager_handle_users", awf.getControlValue());
            continue;
          }
          if ("complaint_consumer_service_confirm".equals(awf.getControlId()) && "审批意见".equals(awf.getControlLable())) {
            if (awf.getControlValue().equals("0")) {
              //同意
              variables.put("complaint_consumer_service_confirm_flow ", "1");
            } else {
              //不同意

              Map<String, Object> variableMap = getVariableByTaskId(taskID, false);
              //指派他人
              if (variableMap.getOrDefault("complaint_operation_manager_check_flow", "").equals(1)) {
                //complaint_consumer_service_confirm_flow
                variables.put("complaint_consumer_service_confirm_flow ", "3");
              } else if (variableMap.getOrDefault("complaint_operation_manager_check_flow", "").equals(2)) {
                //自行处理
                variables.put("complaint_consumer_service_confirm_flow ", "2");
              } else if (variableMap.getOrDefault("complaint_consumer_service_check_flow", "").equals(3)) {
                //代理商处理 ${complaint_consumer_service_check_flow == '3'}
                variables.put("complaint_consumer_service_confirm_flow ", "4");
              }
            }
            continue;
          }
          if ("complaint_consumer_service_check".equals(awf.getControlId()) && "处理类型".equals(awf.getControlLable())) {
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
      //带参数完成任务
      log.info("variables: " + JSON.toJSONString(variables));
      taskService.setVariables(taskID, variables);
      //false 全局变量 直接complete的话,同名的变量好像无法覆盖
//      taskService.complete(taskID, variables);
    }
    taskService.complete(taskID);
    return i;
  }


  /**
   * 退回到上一节点
   */
  @Override
  public void backProcess(String taskID) throws Exception {
    Task task = taskRuntime.task(taskID);
    String processInstanceId = task.getProcessInstanceId();
    // 取得所有历史任务按时间降序排序
    List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstanceId)
            .orderByTaskCreateTime()
            .desc()
            .list();
    if (ObjectUtils.isEmpty(htiList) || htiList.size() < 2) {
      return;
    }
    // list里的第二条代表上一个任务
    HistoricTaskInstance lastTask = htiList.get(1);
    // list里第二条代表当前任务
    HistoricTaskInstance curTask = htiList.get(0);
    // 当前节点的executionId
    String curExecutionId = curTask.getExecutionId();
    // 上个节点的taskId
    String lastTaskId = lastTask.getId();
    // 上个节点的executionId
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
        // 得到ActivityId，只有HistoricActivityInstance对象里才有此方法
        lastActivityId = hai.getActivityId();
        break;
      }
    }
    // 得到上个节点的信息
    FlowNode lastFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(lastActivityId);
    String name = lastFlowNode.getName();
    System.out.println("回退到上个节点:" + name);
    // 取得当前节点的信息
    Execution execution = runtimeService.createExecutionQuery().executionId(curExecutionId).singleResult();
    String curActivityId = execution.getActivityId();
    FlowNode curFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(curActivityId);
    //记录当前节点的原活动方向
    List<SequenceFlow> oriSequenceFlows = new ArrayList<>(curFlowNode.getOutgoingFlows());
    //清理活动方向
    curFlowNode.getOutgoingFlows().clear();
    //建立新方向
    List<SequenceFlow> newSequenceFlowList = new ArrayList<>();
    SequenceFlow newSequenceFlow = new SequenceFlow();
    newSequenceFlow.setId("newSequenceFlowId");
    newSequenceFlow.setSourceFlowElement(curFlowNode);
    newSequenceFlow.setTargetFlowElement(lastFlowNode);
    newSequenceFlowList.add(newSequenceFlow);
    curFlowNode.setOutgoingFlows(newSequenceFlowList);
    // 完成任务
    taskService.complete(task.getId());
    //恢复原方向
    curFlowNode.setOutgoingFlows(oriSequenceFlows);
    org.activiti.engine.task.Task nextTask = taskService
            .createTaskQuery().processInstanceId(processInstanceId).singleResult();
    // 设置执行人
    if (nextTask != null) {
      taskService.setAssignee(nextTask.getId(), lastTask.getAssignee());
    }
  }


  /**
   * 结束任务
   *
   * @param taskId 当前任务ID
   * @param isEnd  true 表示结束节点, false表示start节点
   */
  @Override
  public void endTask(String taskId, boolean isEnd) {
    //  当前任务
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

    //  临时保存当前活动的原始方向
    List originalSequenceFlowList = new ArrayList<>();
    originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
    //  清理活动方向
    currentFlowNode.getOutgoingFlows().clear();

    //  建立新方向
    SequenceFlow newSequenceFlow = new SequenceFlow();
    newSequenceFlow.setId("newSequenceFlowId");
    newSequenceFlow.setSourceFlowElement(currentFlowNode);
    newSequenceFlow.setTargetFlowElement(targetFlowNode);
    List newSequenceFlowList = new ArrayList<>();
    newSequenceFlowList.add(newSequenceFlow);
    //  当前节点指向新的方向
    currentFlowNode.setOutgoingFlows(newSequenceFlowList);
    //  完成当前任务
    taskService.complete(task.getId());
    //如果是end,  可以不用恢复原始方向，不影响其它的流程
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
