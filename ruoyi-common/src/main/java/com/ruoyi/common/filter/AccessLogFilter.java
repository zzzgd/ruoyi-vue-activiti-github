package com.ruoyi.common.filter;

import com.alibaba.fastjson.JSON;
import com.ruoyi.common.utils.ServletUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;


/**
 * @author zzzgd 打印请求信息
 */
@WebFilter(filterName = "AccessLogFilter", urlPatterns = "/*")
@Component
@Order(0)
public class AccessLogFilter implements Filter {

  private Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

  private static final String IP = "IP";
  private static final String REQUEST_ID_HEADER = "RequestId";

  @Override
  public void init(FilterConfig filterConfig) {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {

    //使用request装饰者,确保参数可以重复读取


    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    boolean isMultiPart = request instanceof MultipartRequest;
    HttpServletRequest requestWrapper = new SaveBodyRequestWrapper(request);
    SaveBodyResponseWrapper responseWrapper = new SaveBodyResponseWrapper(response);

    String contextPath = requestWrapper.getContextPath();
    String uri = requestWrapper.getRequestURI();
    String method = requestWrapper.getMethod();

    String[] ignoreUri = {"/",
            "**/*.html",
            "**/*.html",
            "**/*.jpg",
            "**/*.png",
            "/layui/**", "/js/**", "/static/**", "/css/**", "/favicon.ico",
            //静态资源
            "/fonts/**",
            "/swagger-ui.html*/**",
            "/webjars/**",
            //swagger api和swagger-ui
            "/v2/api-docs",
            "/swagger-resources/configuration/ui",
            "/swagger-resources",
            "/swagger-resources/configuration/security",
            "/swagger-ui.html",
            "/csrf"};

    boolean match = false;
    PathMatcher matcher = new AntPathMatcher();
    for (String s : ignoreUri) {
      if (matcher.match(contextPath + s, uri)) {
        match = true;
        break;
      }
    }


    if (match) {
      //直接放行,不过滤
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    // 请求ID，这个是我业务中的id，大家可自行决定是否需要
    // 请求进入时间
    long start = System.currentTimeMillis();
    Object requestId = requestWrapper.getHeader(REQUEST_ID_HEADER);
    if (requestId == null) {
      requestId = ThreadLocalRandom.current().nextLong(100000000000000000L, 1000000000000000000L);
    }

    MDC.put(REQUEST_ID_HEADER, requestId + "");
    responseWrapper.setHeader(REQUEST_ID_HEADER, requestId + "");


    String str = String.format("【request】|uri:%s(%s)", uri, method);

    StringBuilder sb = new StringBuilder(str);

    boolean fileRequest = isMultiPart || (requestWrapper.getContentType() != null && requestWrapper.getContentType().contains(MULTIPART_FORM_DATA.getType()));
    if (!fileRequest) {

      Map<String, String[]> map = requestWrapper.getParameterMap();
      if (MapUtils.isNotEmpty(map)) {
        sb.append("|queryUrl: 【").append(requestWrapper.getRequestURL()).append("?").append(requestWrapper.getQueryString()).append("】").toString();

        sb.append("\t\t|query: 【");
        sb.append(JSON.toJSONString(map));
        sb.append("】");
      }


      // 打印请求json参数，如果是上传下载则不打印
      try (InputStream inputStream = requestWrapper.getInputStream()) {
        String logContext = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        if (StringUtils.isNotBlank(logContext)) {
          sb.append("\t\t|body: 【").append(logContext.replaceAll("[\r\n]", "")).append("】");
        }
      } catch (IOException ioe1) {
        log.error("【filter】filter异常 ", ioe1);
      }
    } else {
      log.info("【filter】上传文件");
    }
    log.info(sb.toString());

    int responseCode = 200;
    String errMsg = "";
    try {
      //设置属性为包装类
      RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestWrapper, responseWrapper));
      //将request的装饰类传进去
      filterChain.doFilter(requestWrapper, responseWrapper);
      // 返回的结果
      responseCode = responseWrapper.getStatus();
      String contentType = responseWrapper.getContentType();
      if (StringUtils.isNotBlank(contentType) && APPLICATION_JSON.getType().equals(MediaType.parseMediaType(contentType).getType())) {
        try {
          String content = new String(responseWrapper.getTextContent().getBytes(), StandardCharsets.UTF_8);
          //最长2048个字符
          log.info("response:{}", content.length() > 2048 ? StringUtils.substring(content, 0, 2048) + "....." : content);
        } catch (Exception ignored) {
        }
      }
      copyResponse(responseWrapper, response);
    } catch (Exception e) {
      log.error("id:{} 出现异常 ", requestId, e);
      ServletUtils.renderString(response, e.getMessage());
    } finally {
      log.info("【response】|uri:{}({})|status:{}|cost:{}ms ", uri, method, responseCode, System.currentTimeMillis() - start);
    }
  }

  @Override
  public void destroy() {

  }

  private void copyResponse(SaveBodyResponseWrapper responseWrapper, HttpServletResponse response) throws IOException {
    response.setStatus(responseWrapper.getStatus());
    response.setContentType(responseWrapper.getContentType());
    Collection<String> headerNames = responseWrapper.getHeaderNames();
    if (headerNames != null) {
      for (String headerName : headerNames) {
        response.setHeader(headerName, responseWrapper.getHeader(headerName));
      }
    }
    response.setCharacterEncoding("UTF-8");
    response.getOutputStream().write(responseWrapper.getByteArrayOutputStream().toByteArray());
  }

}