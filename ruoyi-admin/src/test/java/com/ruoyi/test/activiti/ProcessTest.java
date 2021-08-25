package com.ruoyi.test.activiti;

import com.ruoyi.activiti.service.IActTaskService;
import com.ruoyi.test.BaseTest;
import com.ruoyi.test.util.SecurityUtil;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * ProcessTest
 *
 * @author zgd
 * @date 2021/8/17 11:54
 */

public class ProcessTest  extends BaseTest {
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
  public void queryProcessByName(){
    List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().startedBy("guanxing").list();
    for (HistoricProcessInstance historicProcessInstance : list) {
      System.out.println(historicProcessInstance.getProcessDefinitionId());
      System.out.println(historicProcessInstance.getName());
      System.out.println(historicProcessInstance.getProcessDefinitionKey());
      System.out.println("-----");
    }

  }

}
