package com.ruoyi.activiti.service.image;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.history.HistoricActivityInstance;

import java.util.List;
import java.util.Set;

/**
 * ProcessTranceService
 *
 * @author zgd
 * @date 2021/8/17 18:11
 */
public interface ProcessTraceService {


  List<String> getHighLine(BpmnModel bpmnModel, String instanceId);
}
