package com.ruoyi.workflow.service.impl;

import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.system.service.ISysUserService;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.mapper.WorkflowCustomerComplaintMapper;
import com.ruoyi.workflow.domain.WorkflowCustomerComplaint;
import com.ruoyi.workflow.service.IWorkflowCustomerComplaintService;
import org.springframework.transaction.annotation.Transactional;

import static com.ruoyi.activiti.constant.ActivitiConst.PROCESS_KEY_CUSTOMER_COMPLAINT;

/**
 * 客诉工单工作流Service业务层处理
 *
 * @author ruoyi
 * @date 2021-08-12
 */
@Service
public class WorkflowCustomerComplaintServiceImpl implements IWorkflowCustomerComplaintService {
  private static final Logger log = LoggerFactory.getLogger(WorkflowCustomerComplaintServiceImpl.class);

  @Autowired
  private WorkflowCustomerComplaintMapper workflowCustomerComplaintMapper;
  @Autowired
  private ISysUserService sysUserService;
  @Autowired
  private ProcessRuntime processRuntime;
  @Autowired
  private TaskService taskService;
  @Autowired
  private TaskRuntime taskRuntime;

  /**
   * 查询客诉工单工作流
   *
   * @param id 客诉工单工作流ID
   * @return 客诉工单工作流
   */
  @Override
  public WorkflowCustomerComplaint selectWorkflowCustomerComplaintById(Long id) {
    return workflowCustomerComplaintMapper.selectWorkflowCustomerComplaintById(id);
  }

  @Override
  public WorkflowCustomerComplaint selectWorkflowCustomerComplaintByInstanceId(String instanceId) {
    return workflowCustomerComplaintMapper.selectWorkflowCustomerComplaintByInstanceId(instanceId);
  }

  @Override
  public WorkflowCustomerComplaint selectWorkflowCustomerComplaintByBusinessKey(String businessKey) {
    WorkflowCustomerComplaint complaint = workflowCustomerComplaintMapper.selectWorkflowCustomerComplaintByBusinessKey(businessKey);
    return complaint;
  }

  /**
   * 查询客诉工单工作流列表 complaint = {WorkflowCustomerComplaint@14755} "WorkflowCustomerComplaint{id=5, serialNo='null', instanceId='null',
   * businessKey='null', type='yunying', commitTime=null, state=0}"
   *
   * @param workflowCustomerComplaint 客诉工单工作流
   * @return 客诉工单工作流
   */
  @Override
  public List<WorkflowCustomerComplaint> selectWorkflowCustomerComplaintList(WorkflowCustomerComplaint workflowCustomerComplaint) {
    return workflowCustomerComplaintMapper.selectWorkflowCustomerComplaintList(workflowCustomerComplaint);
  }

  /**
   * 新增客诉工单工作流
   *
   * @param workflowCustomerComplaint 客诉工单工作流
   * @return 结果
   */
  @Override
  @Transactional
  public int insertWorkflowCustomerComplaint(WorkflowCustomerComplaint workflowCustomerComplaint) {
    String id = UUID.randomUUID().toString();
    org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(SecurityUtils.getUsername());

    ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
            .start()
            .withProcessDefinitionKey(PROCESS_KEY_CUSTOMER_COMPLAINT)
            .withName("客诉工单" + workflowCustomerComplaint.getSerialNo())
            .withBusinessKey(id)
            //工单岗审核
            .withVariable("complaint_consumer_service_check_users", "gongdan_sh")
            //运营审核
            .withVariable("complaint_operation_manager_check_users", "yunying_sh")
            //代理商处理
            .withVariable("complaint_agent_handle_users", "dailishang")
            //运营商处理
            .withVariable("complaint_operation_manager_handle_users", "yunying_cl")
            //工单岗确认
            .withVariable("complaint_consumer_service_confirm_users", "gongdan_qr")
            //座席回访
            .withVariable("complaint_agent_follow_up_users", "zuoxihuifang")
            //审核类型
//            .withVariable("complaint_consumer_service_check_flow", getFlowTypeFromApproveType(workflowCustomerComplaint.getType()))
            .build());
    workflowCustomerComplaint.setCreateTime(DateUtils.getNowDate());
    workflowCustomerComplaint.setCreateBy(SecurityUtils.getUsername());
    workflowCustomerComplaint.setState(0);
    workflowCustomerComplaint.setBusinessKey(id);
    workflowCustomerComplaint.setInstanceId(processInstance.getId());
    int i = workflowCustomerComplaintMapper.insertWorkflowCustomerComplaint(workflowCustomerComplaint);
    log.info("启动流程成功. 流程id:{}|businessKey:{}", processInstance.getId(), id);
    return i;
  }


  private int getFlowTypeFromApproveType(String type) {
    switch (type) {
      case "yunying":
        return 1;
      case "zuoxi":
        return 2;
      case "dailishang":
        return 3;
    }
    return 0;
  }

  /**
   * 修改客诉工单工作流
   *
   * @param workflowCustomerComplaint 客诉工单工作流
   * @return 结果
   */
  @Override
  public int updateWorkflowCustomerComplaint(WorkflowCustomerComplaint workflowCustomerComplaint) {
    workflowCustomerComplaint.setUpdateTime(DateUtils.getNowDate());
    return workflowCustomerComplaintMapper.updateWorkflowCustomerComplaint(workflowCustomerComplaint);
  }

  /**
   * 批量删除客诉工单工作流
   *
   * @param ids 需要删除的客诉工单工作流ID
   * @return 结果
   */
  @Override
  public int deleteWorkflowCustomerComplaintByIds(Long[] ids) {
    return workflowCustomerComplaintMapper.deleteWorkflowCustomerComplaintByIds(ids);
  }

  /**
   * 删除客诉工单工作流信息
   *
   * @param id 客诉工单工作流ID
   * @return 结果
   */
  @Override
  public int deleteWorkflowCustomerComplaintById(Long id) {
    return workflowCustomerComplaintMapper.deleteWorkflowCustomerComplaintById(id);
  }
}
