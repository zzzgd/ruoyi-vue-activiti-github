package com.ruoyi.workflow.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客诉工单工作流对象 workflow_customer_complaint
 * 
 * @author ruoyi
 * @date 2021-08-12
 */
public class WorkflowCustomerComplaint extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工单单号 */
    @Excel(name = "工单单号")
    private String serialNo;

    /** 流程实例id */
    @Excel(name = "流程实例id")
    private String instanceId;

    /** 流程实例id */
    @Excel(name = "流程key")
    private String businessKey;

    /** 审批类型 */
    @Excel(name = "审批类型")
    private String type;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date commitTime;

    /** 状态 */
    @Excel(name = "状态")
    private Integer state;



    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setSerialNo(String serialNo) 
    {
        this.serialNo = serialNo;
    }

    public String getSerialNo() 
    {
        return serialNo;
    }
    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }
    public void setCommitTime(Date commitTime) 
    {
        this.commitTime = commitTime;
    }

    public Date getCommitTime() 
    {
        return commitTime;
    }
    public void setState(Integer state)
    {
        this.state = state;
    }

    public Integer getState()
    {
        return state;
    }
    public void setInstanceId(String instanceId) 
    {
        this.instanceId = instanceId;
    }

    public String getInstanceId() 
    {
        return instanceId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    @Override
    public String toString() {
        return "WorkflowCustomerComplaint{" +
                "id=" + id +
                ", serialNo='" + serialNo + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", businessKey='" + businessKey + '\'' +
                ", type='" + type + '\'' +
                ", commitTime=" + commitTime +
                ", state=" + state +
                '}';
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }
}
