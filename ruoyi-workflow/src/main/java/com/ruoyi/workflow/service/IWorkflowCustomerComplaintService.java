package com.ruoyi.workflow.service;

import java.util.List;
import com.ruoyi.workflow.domain.WorkflowCustomerComplaint;

/**
 * 客诉工单工作流Service接口
 * 
 * @author ruoyi
 * @date 2021-08-12
 */
public interface IWorkflowCustomerComplaintService 
{
    /**
     * 查询客诉工单工作流
     * 
     * @param id 客诉工单工作流ID
     * @return 客诉工单工作流
     */
    public WorkflowCustomerComplaint selectWorkflowCustomerComplaintById(Long id);

    WorkflowCustomerComplaint selectWorkflowCustomerComplaintByInstanceId(String instanceId);
    WorkflowCustomerComplaint selectWorkflowCustomerComplaintByBusinessKey(String businessKey);
    /**
     * 查询客诉工单工作流列表
     * 
     * @param workflowCustomerComplaint 客诉工单工作流
     * @return 客诉工单工作流集合
     */
    public List<WorkflowCustomerComplaint> selectWorkflowCustomerComplaintList(WorkflowCustomerComplaint workflowCustomerComplaint);

    /**
     * 新增客诉工单工作流
     * 
     * @param workflowCustomerComplaint 客诉工单工作流
     * @return 结果
     */
    public int insertWorkflowCustomerComplaint(WorkflowCustomerComplaint workflowCustomerComplaint);

    /**
     * 修改客诉工单工作流
     * 
     * @param workflowCustomerComplaint 客诉工单工作流
     * @return 结果
     */
    public int updateWorkflowCustomerComplaint(WorkflowCustomerComplaint workflowCustomerComplaint);

    /**
     * 批量删除客诉工单工作流
     * 
     * @param ids 需要删除的客诉工单工作流ID
     * @return 结果
     */
    public int deleteWorkflowCustomerComplaintByIds(Long[] ids);

    /**
     * 删除客诉工单工作流信息
     * 
     * @param id 客诉工单工作流ID
     * @return 结果
     */
    public int deleteWorkflowCustomerComplaintById(Long id);
}
