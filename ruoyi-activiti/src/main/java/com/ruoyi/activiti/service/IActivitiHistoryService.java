package com.ruoyi.activiti.service;

import com.ruoyi.activiti.domain.dto.ActivitiHighLineDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IActivitiHistoryService {
    public ActivitiHighLineDTO gethighLine(String instanceId);
}
