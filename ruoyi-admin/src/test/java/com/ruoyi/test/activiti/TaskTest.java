package com.ruoyi.test.activiti;

import com.alibaba.fastjson.JSON;
import com.ruoyi.activiti.service.IActTaskService;
import com.ruoyi.test.BaseTest;
import com.ruoyi.test.util.SecurityUtil;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.task.IdentityLink;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.ruoyi.activiti.constant.ActivitiConst.PROCESS_KEY_CUSTOMER_COMPLAINT;

/**
 * TaskTest
 *
 * @author zgd
 * @date 2021/8/10 12:36
 */
public class TaskTest extends BaseTest {
  @Autowired
  public ProcessRuntime processRuntime;
  @Autowired
  public TaskRuntime taskRuntime;
  @Autowired
  public TaskService tasksService;
  @Autowired
  public IActTaskService actTaskService;
  @Autowired
  public SecurityUtil securityUtil;
  @Autowired
  public HistoryService historyService;
  @Autowired
  public ProcessEngine processEngine;

  @Test
  public void queryMyTask() {
    //如果流程图设置的是候选人,或者是候选组, 那么act_task中的assignee虽然为null,但是人仍然可以查到记录
    securityUtil.logInAs("zhugeliang");
    //设置任务负责人 ACT_RUN_TASK
    Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
    if (tasks.getTotalItems() > 0) {
      List<Task> content = tasks.getContent();
      for (Task task : content) {
        System.out.println("任务ID:" + task.getId());
        System.out.println("任务名称:" + task.getName());
        System.out.println("任务的创建时间:" + task.getCreatedDate());
        System.out.println("任务的办理人:" + task.getAssignee());
        System.out.println("流程实例ID：" + task.getProcessInstanceId());
        System.out.println("流程定义ID:" + task.getProcessDefinitionId());
        System.out.println("getOwner:" + task.getOwner());
        System.out.println("getDescription:" + task.getDescription());
        System.out.println("getFormKey:" + task.getFormKey());
        System.out.println("------------------------");

      }
    }
  }


  @Test
  public void queryMyTask2() {
    //根据任务名称或者id来查
    List<org.activiti.engine.task.Task> list = tasksService.createTaskQuery()
            .taskName("f3649267-fa59-11eb-aa66-00ff64da1685")
            .taskAssignee("zhugeliang")
            .list();
    //设置任务负责人 ACT_RUN_TASK
    for (org.activiti.engine.task.Task task : list) {
      System.out.println("任务ID:" + task.getId());
      System.out.println("任务名称:" + task.getName());
      System.out.println("任务的创建时间:" + task.getDueDate());
      System.out.println("任务的办理人:" + task.getAssignee());
      System.out.println("流程实例ID：" + task.getProcessInstanceId());
      System.out.println("流程定义ID:" + task.getProcessDefinitionId());
      System.out.println("getOwner:" + task.getOwner());
      System.out.println("getDescription:" + task.getDescription());
      System.out.println("getFormKey:" + task.getFormKey());
      System.out.println("------------------------");
    }
  }


  /**
   * 驳回
   */
  @Test
  public void backTask() throws Exception {
    //要指定一个认证通过的对象, 并且这个人还必须是任务的持有者
    securityUtil.logInAs("gongdan_qr");
    String taskId = "84495511-ff10-11eb-bff4-00ff64da1685";
    actTaskService.backProcess(taskId);
    System.out.println("回退成功");
  }


  @Test
  public void endTask() {
    //要指定一个认证通过的对象, 并且这个人还必须是任务的持有者
    securityUtil.logInAs("guanxing");
    String taskId = "de2ebb2b-fe5d-11eb-b26b-00ff64da1685";
    actTaskService.endTask(taskId, true);
  }


  @Test
  public void testStartProcess() {
    securityUtil.logInAs("zhangsan");
    ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
            .start()
            .withProcessDefinitionKey(PROCESS_KEY_CUSTOMER_COMPLAINT)
            .withName("客诉工单_测试" + new Date().toString())
            .withBusinessKey(UUID.randomUUID().toString())
            //工单岗审核
            .withVariable("complaint_consumer_service_check_users", "lisi")
            //运营审核
            .withVariable("complaint_operation_manager_check_users", "wangwu")
            //代理商处理
            .withVariable("complaint_agent_handle_users", "dailishang")
            //运营商处理
            .withVariable("complaint_operation_manager_handle_users", "yunying_cl")
            //工单岗确认
            .withVariable("complaint_consumer_service_confirm_users", "gongdan_qr")
            //座席回访
            .withVariable("complaint_agent_follow_up_users", "zuoxihuifang")
            .withVariable("test_quanju", "init")
            //审核类型
//            .withVariable("complaint_consumer_service_check_flow", getFlowTypeFromApproveType(workflowCustomerComplaint.getType()))
            .build());
    System.out.println("processInstance.getProcessDefinitionId() = " + processInstance.getProcessDefinitionId());
    System.out.println("processInstance.getId() = " + processInstance.getId());
  }


  /**
   * 运营处理
   */
  @Test
  public void completeTask() {
    securityUtil.logInAs("lisi");
    String taskId = "bfb5a7d6-ff03-11eb-a2ab-00ff64da1685";
    HashMap<String, Object> variables = new HashMap<String, Object>();
    //运营处理
    variables.put("complaint_consumer_service_check_flow", "1");
    variables.put("complaint_operation_manager_handle_users", "zhugeliang");
    tasksService.complete(taskId, variables);
  }

  @Test
  public void completeWithVariable() {
    securityUtil.logInAs("wangwu");
    String taskId = "e8ac7a4d-fe92-11eb-bb0f-00ff64da1685";
    HashMap<String, Object> variables = new HashMap<String, Object>();
    //运营处理
    variables.put("complaint_consumer_service_check_flow", "1");
    //指派他人
    variables.put("complaint_operation_manager_check_flow", "1");
    variables.put("complaint_operation_manager_handle_users", "zhangfei");

    tasksService.setVariable(taskId,"test_quanju","tasksService.setVariable第三次更改后");

    tasksService.complete(taskId, variables,false);
  }


  @Test
  public void queryVariable() {
    String taskId = "3c3cdfca-fe90-11eb-938b-00ff64da1685";
    Map<String, Object> variablesMap = tasksService.getVariablesLocal(taskId);
    System.out.println(JSON.toJSONString(variablesMap));
    Map<String, Object> variablesMap2 = tasksService.getVariables(taskId);
    System.out.println(JSON.toJSONString(variablesMap2));
  }
}
