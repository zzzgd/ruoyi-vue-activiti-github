package com.ruoyi.test.activiti;

import com.ruoyi.activiti.service.image.ProcessImageService;
import com.ruoyi.activiti.util.SvgUtil;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.test.BaseTest;
import com.ruoyi.test.util.SecurityUtil;
import org.activiti.engine.RepositoryService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.InputStream;
import java.security.Security;
import java.time.Instant;

/**
 * ImageTest
 *
 * @author zgd
 * @date 2021/8/17 17:56
 */
public class ImageTest extends BaseTest {


  @Autowired
  ProcessImageService imageService;

  @Autowired
  RepositoryService repositoryService;
  @Autowired
  public SecurityUtil securityUtil;

  @Test
  public void test1() throws Exception {
    // 85ab9218-2eee-11eb-801d-d4f5ef050be7
    securityUtil.logInAs("guanxing");

    InputStream image = imageService.getFlowImgByProcInstId("61c5d0d0-ff07-11eb-83da-00ff64da1685");
    String imageName = Instant.now().getEpochSecond() + ".svg";
    FileUtils.copyInputStreamToFile(image, new File("src/main/resources/processes/" + imageName));// 绘图
  }

  @Test
  public void fun02() throws Exception {
    // 85ab9218-2eee-11eb-801d-d4f5ef050be7
    securityUtil.logInAs("guanxing");
    InputStream image = imageService.getFlowImgByProcInstId("61c5d0d0-ff07-11eb-83da-00ff64da1685");
    String imageName = Instant.now().getEpochSecond() + ".png";
    SvgUtil.svg2Png(image, "src/main/resources/processes/" + imageName);
  }
}
