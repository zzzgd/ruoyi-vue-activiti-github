package com.ruoyi.activiti.service.impl;

import com.ruoyi.activiti.domain.ActWorkflowFormData;
import com.ruoyi.activiti.domain.dto.HistoryFormDataDTO;
import com.ruoyi.activiti.domain.dto.HistoryDataDTO;
import com.ruoyi.activiti.service.IActWorkflowFormDataService;
import com.ruoyi.activiti.service.IFormHistoryDataService;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.system.service.ISysDictDataService;
import com.ruoyi.system.service.ISysDictTypeService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 汇讯数码科技(深圳)有限公司 创建日期:2020/11/3-10:20 版本   开发者     日期 1.0    Danny    2020/11/3
 */
@Service
public class FormHistoryDataServiceImpl implements IFormHistoryDataService {
  @Autowired
  private IActWorkflowFormDataService actWorkflowFormDataService;
  @Autowired
  private ISysDictTypeService sysDictTypeService;
  @Autowired
  private ISysDictDataService sysDictDataService;


  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Override
  public List<HistoryDataDTO> historyDataShow(String businessKey) {
    List<HistoryDataDTO> returnHistoryFromDataDTOS = new ArrayList<>();
    List<ActWorkflowFormData> actWorkflowFormData = actWorkflowFormDataService.selectActWorkflowFormDataByBusinessKey(businessKey);
    Map<String, List<ActWorkflowFormData>> collect = actWorkflowFormData.stream().collect(Collectors.groupingBy(ActWorkflowFormData::getTaskNodeName));
    collect.entrySet().forEach(
            entry -> {
              HistoryDataDTO returnHistoryFromDataDTO = new HistoryDataDTO();
              returnHistoryFromDataDTO.setTaskNodeName(entry.getValue().get(0).getTaskNodeName());
              returnHistoryFromDataDTO.setCreateName(entry.getValue().get(0).getCreateName());
              returnHistoryFromDataDTO.setCreatedDate(sdf.format(entry.getValue().get(0).getCreateTime()));
              returnHistoryFromDataDTO.setFormHistoryDataDTO(entry.getValue().stream().map(this::getFormatHistoryDataDtoByActFormData)
                      .collect(Collectors.toList()));
              returnHistoryFromDataDTOS.add(returnHistoryFromDataDTO);
            }
    );
    List<HistoryDataDTO> collect1 = returnHistoryFromDataDTOS.stream().sorted((x, y) -> x.getCreatedDate().compareTo(y.getCreatedDate())).collect(Collectors.toList());

    return collect1;
  }

  private HistoryFormDataDTO getFormatHistoryDataDtoByActFormData(ActWorkflowFormData awfd) {
    String value;
    if ("radio".equals(awfd.getControlType())) {
      int i = Integer.parseInt(awfd.getControlValue());
      value = awfd.getControlDefault().split("--__--")[i];
    } else if ("select".equals(awfd.getControlType())) {
      value = sysDictDataService.selectDictLabel(awfd.getControlDefault(), awfd.getControlValue());
    } else {
      value = awfd.getControlValue();
    }
    return new HistoryFormDataDTO(awfd.getControlName(), value);
  }


}
