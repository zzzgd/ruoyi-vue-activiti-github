package com.ruoyi.activiti.manage;

import com.ruoyi.activiti.util.VerificationUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ProcessHistoryManager {
    private Logger log = LoggerFactory.getLogger(ProcessHistoryManager.class);

    HistoryService historyService;

    @Autowired
    public ProcessHistoryManager(HistoryService historyService) {
        this.historyService = historyService;
    }


    /**
     * Desc: 通过流程实例ID获取历史流程实例
     *
     * @param processInstanceId 流程实例Id
     * @return 历史流程实例
     */
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return VerificationUtils.checkHistoricProcessInstanceById(processInstanceId);
    }


    /**
     * Desc: 通过流程实例ID获取已经完成的历史流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 已经完成的历史流程实例
     */
    public List<HistoricProcessInstance> getHistoricFinishedProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .list();
    }



    /**
     * 获取已完成的活动id
     *
     * @param instanceId
     * @return
     */
    public Set<String> getFinishedActivityId(String instanceId) {
        //获取流程实例 历史节点（已完成）
        List<HistoricActivityInstance> listFinished = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId)
                .finished()
                .list();
        //高亮节点ID
        Set<String> highPoint = new HashSet<>();
        listFinished.forEach(s -> highPoint.add(s.getActivityId()));
        return highPoint;
    }


    public List<HistoricActivityInstance> getUnfinishedActiviti(String instanceId) {
        //获取流程实例 历史节点（待办节点）
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId)
                .unfinished()
                .list();
    }

    /**
     * 根据节点的开始时间排序
     * @param instanceId
     * @return
     */
    public List<HistoricActivityInstance> getHistoryActivitiByStartTimeAsc(String instanceId) {
        //获取流程实例 历史节点(全部)
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
    }


}