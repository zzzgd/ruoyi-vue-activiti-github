package com.ruoyi.test.activiti;
import com.ruoyi.test.BaseTest;
import com.ruoyi.test.util.SecurityUtil;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * TaskTest
 *
 * @author zgd
 * @date 2021/8/10 12:36
 */
public class TaskTest extends BaseTest{
  @Autowired
  public ProcessRuntime processRuntime;
  @Autowired
  public TaskRuntime taskRuntime;
  @Autowired
  public TaskService tasksService;
  @Autowired
  public SecurityUtil securityUtil;
  @Autowired
  public HistoryService historyService;
  @Autowired
  public ProcessEngine processEngine;

  @Test
  public void queryMyTask(){
    //如果流程图设置的是候选人,或者是候选组, 那么act_task中的assignee虽然为null,但是人仍然可以查到记录
    securityUtil.logInAs("ry");
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
      }
    }
  }

}
