package com.ruoyi.workflow.controller;

import java.util.List;

import com.ruoyi.workflow.domain.WorkflowCustomerComplaint;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.workflow.service.IWorkflowCustomerComplaintService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 客诉工单工作流Controller
 * 
 * @author ruoyi
 * @date 2021-08-12
 */
@RestController
@RequestMapping("/workflow/customercomplaint")
public class WorkflowCustomerComplaintController extends BaseController
{
    @Autowired
    private IWorkflowCustomerComplaintService workflowCustomerComplaintService;

    /**
     * 查询客诉工单工作流列表
     */
    @PreAuthorize("@ss.hasPermi('workflow:customercomplaint:list')")
    @GetMapping("/list")
    public TableDataInfo list(WorkflowCustomerComplaint workflowCustomerComplaint)
    {
        startPage();
        List<WorkflowCustomerComplaint> list = workflowCustomerComplaintService.selectWorkflowCustomerComplaintList(workflowCustomerComplaint);
        return getDataTable(list);
    }

    /**
     * 导出客诉工单工作流列表
     */
    @PreAuthorize("@ss.hasPermi('workflow:customercomplaint:export')")
    @Log(title = "客诉工单工作流", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(WorkflowCustomerComplaint workflowCustomerComplaint)
    {
        List<WorkflowCustomerComplaint> list = workflowCustomerComplaintService.selectWorkflowCustomerComplaintList(workflowCustomerComplaint);
        ExcelUtil<WorkflowCustomerComplaint> util = new ExcelUtil<WorkflowCustomerComplaint>(WorkflowCustomerComplaint.class);
        return util.exportExcel(list, "customercomplaint");
    }

    /**
     * 获取客诉工单工作流详细信息
     */
    @PreAuthorize("@ss.hasPermi('workflow:customercomplaint:query')")
    @GetMapping(value = "/{businessKey}")
    public AjaxResult getInfo(@PathVariable("businessKey") String businessKey)
    {
        return AjaxResult.success(workflowCustomerComplaintService.selectWorkflowCustomerComplaintByBusinessKey(businessKey));
    }

    /**
     * 新增客诉工单工作流
     */
    @PreAuthorize("@ss.hasPermi('workflow:customercomplaint:add')")
    @Log(title = "客诉工单工作流", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WorkflowCustomerComplaint workflowCustomerComplaint)
    {
        return toAjax(workflowCustomerComplaintService.insertWorkflowCustomerComplaint(workflowCustomerComplaint));
    }

    /**
     * 修改客诉工单工作流
     */
    @PreAuthorize("@ss.hasPermi('workflow:customercomplaint:edit')")
    @Log(title = "客诉工单工作流", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WorkflowCustomerComplaint workflowCustomerComplaint)
    {
        return toAjax(workflowCustomerComplaintService.updateWorkflowCustomerComplaint(workflowCustomerComplaint));
    }

    /**
     * 删除客诉工单工作流
     */
    @PreAuthorize("@ss.hasPermi('workflow:customercomplaint:remove')")
    @Log(title = "客诉工单工作流", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(workflowCustomerComplaintService.deleteWorkflowCustomerComplaintByIds(ids));
    }
}
