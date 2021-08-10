package com.ruoyi.common.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Enumeration;


/**
 * request包装类
 *
 * @author zzzgd
 */
public class SaveBodyRequestWrapper extends HttpServletRequestWrapper {
  private Logger log = LoggerFactory.getLogger(SaveBodyRequestWrapper.class);

  private byte[] body;

  private final HttpServletRequest request;

  public SaveBodyRequestWrapper(HttpServletRequest request) {
    //request.getParameter 和 getInputStream 只能使用一次. 所以在我们将inputstream使用byte数组存起来,并且构造方法中不做操作,
    //待程序后面调用getParameter以后,再来调用inputstream就都有值了
    super(request);
    this.request = request;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  @Override
  public ServletInputStream getInputStream() {
    if (body == null){
      byte[] bytes = new byte[0];
      try (InputStream inputStream = request.getInputStream()) {
        bytes = StreamUtils.copyToByteArray(inputStream);
      } catch (IOException e) {
        log.error("读取流异常 ",e);
      }
      body = bytes;
    }
    final ByteArrayInputStream bais = new ByteArrayInputStream(body);

    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return bais.available() <= 0;
      }

      @Override
      public boolean isReady() {
        return bais.available() > 0;
      }

      @Override
      public void setReadListener(ReadListener readListener) {

      }

      @Override
      public int read() {
        return bais.read();
      }
    };
  }

  @Override
  public String getHeader(String name) {
    return super.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return super.getHeaderNames();
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    return super.getHeaders(name);
  }


  @Override
  public String getParameter(String name) {
    return super.getParameter(name);
  }
}