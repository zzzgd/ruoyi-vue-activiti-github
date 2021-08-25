package com.ruoyi.activiti.service.image.impl;

import com.ruoyi.activiti.manage.ProcessHistoryManager;
import com.ruoyi.activiti.service.image.ProcessTraceService;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ProcessTraceServiceImpl
 *
 * @author zgd
 * @date 2021/8/17 18:12
 */
@Service
public class ProcessTraceServiceImpl implements ProcessTraceService {


  @Autowired
  private HistoryService historyService;
  @Autowired
  private ProcessHistoryManager processHistoryManager;




  /**
   * 获取高亮的连线
   * @param bpmnModel
   * @param instanceId
   * @return
   */
  @Override
  public List<String> getHighLine(BpmnModel bpmnModel, String instanceId) {
    List<HistoricActivityInstance> finishedActivityList = processHistoryManager.getHistoryActivitiByStartTimeAsc(instanceId);

    //因为我们这里只定义了一个Process 所以获取集合中的第一个即可
    Process process = bpmnModel.getProcesses().get(0);
    //获取所有的FlowElement信息
    Collection<FlowElement> flowElements = process.getFlowElements();

    Map<String, String> map = new HashMap<>();
    for (FlowElement flowElement : flowElements) {
      //判断是否是连线
      if (flowElement instanceof SequenceFlow) {
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
        String ref = sequenceFlow.getSourceRef();
        String targetRef = sequenceFlow.getTargetRef();
        map.put(ref + targetRef, sequenceFlow.getId());
      }
    }

    //高亮连线ID
    List<String> highLine = new ArrayList<>();
    //各个历史节点   两两组合 key
    Set<String> keyList = new HashSet<>();
    if (finishedActivityList.size() > 1) {
      for (int i = 0; i < finishedActivityList.size() - 1; i++) {
        keyList.add(finishedActivityList.get(i).getActivityId() + finishedActivityList.get(i + 1).getActivityId());
      }
//      for (int i = 0; i < list.size() - 1; i++) {
//        for (int j = i + 1; j < list.size(); j++) {
//          keyList.add(list.get(i).getActivityId() + list.get(j).getActivityId());
//        }
//      }
    }
    keyList.forEach(s -> highLine.add(map.get(s)));

    //需要移除的高亮连线
    Set<String> set = new HashSet<>();

//    List<HistoricActivityInstance> listUnFinished = getUnfinishedActiviti(instanceId);

//    listUnFinished.forEach(s -> {
//      for (FlowElement flowElement : flowElements) {
////判断是否是 用户节点
//        if (flowElement instanceof UserTask) {
//          UserTask userTask = (UserTask) flowElement;
//
//          if (userTask.getId().equals(s.getActivityId())) {
//            List<SequenceFlow> outgoingFlows = userTask.getOutgoingFlows();
//            //因为 高亮连线查询的是所有节点  两两组合 把待办 之后  往外发出的连线 也包含进去了  所以要把高亮待办节点 之后 即出的连线去掉
//            if (outgoingFlows != null && outgoingFlows.size() > 0) {
//              outgoingFlows.forEach(a -> {
//                if (a.getSourceRef().equals(s.getActivityId())) {
//                  set.add(a.getId());
//                }
//              });
//            }
//          }
//        }
//      }
//    });
    highLine.removeAll(set);
    return highLine;
  }

}
