package com.ruoyi.common.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 将response的 数据保存一份
 * 可以重复读取,否则response的数据读取一遍流就关闭了,无法重复读取
 */
public class SaveBodyResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PrintWriter printWriter = new PrintWriter(outputStream);

    public SaveBodyResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }
            @Override
            public void setWriteListener(WriteListener listener) {
            }
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }
        };
    }
    public void flush(){
        try {
            printWriter.flush();
            printWriter.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream getByteArrayOutputStream(){
        return outputStream;
    }

    public String getTextContent() {
        flush();
        return outputStream.toString();
    }
}
