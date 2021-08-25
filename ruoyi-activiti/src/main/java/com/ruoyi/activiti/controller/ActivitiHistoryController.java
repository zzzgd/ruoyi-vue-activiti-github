package com.ruoyi.activiti.controller;


import com.ruoyi.activiti.domain.dto.ActivitiHighLineDTO;
import com.ruoyi.activiti.service.IActivitiHistoryService;
import com.ruoyi.activiti.service.image.ProcessImageService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;


@RestController
@RequestMapping("/activitiHistory")
public class ActivitiHistoryController {

  @Autowired
  private IActivitiHistoryService activitiHistoryService;
  @Autowired
  private ProcessImageService processImageService;

  //流程图高亮
  @GetMapping("/gethighLine")
  public AjaxResult gethighLine(@RequestParam("instanceId") String instanceId) {

    ActivitiHighLineDTO activitiHighLineDTO = activitiHistoryService.gethighLine(instanceId);
    return AjaxResult.success(activitiHighLineDTO);


  }


  @RequestMapping("/gethighLineImg")
  public void processTracking(String processInstanceId, HttpServletResponse response) throws Exception {
    response.setContentType("image/svg-xml");
    response.setHeader("Content-Disposition", "attachment;fileName=流程图" + processInstanceId + ".svg");
    try (InputStream ins = processImageService.getFlowImgByProcInstId(processInstanceId);
         ServletOutputStream out = response.getOutputStream();) {

      int len;
      byte[] b = new byte[1024];
      while ((len = ins.read(b, 0, 1024)) != -1) {
        out.write(b, 0, len);
      }
    }
  }


}
