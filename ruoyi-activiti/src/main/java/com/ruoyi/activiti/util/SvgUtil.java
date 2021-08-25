package com.ruoyi.activiti.util;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.*;

/**
 * SvgUtil
 *
 * @author zgd
 * @date 2021/8/17 18:47
 */
public class SvgUtil {


  public static void svg2Png(File svg, File png) throws IOException, TranscoderException {
    try (InputStream in = new FileInputStream(svg);
         FileOutputStream ou = new FileOutputStream(png);
    ) {
      svg2Png(png, ou);
    }
  }

  public static void svg2Png(File svg, OutputStream out) throws IOException, TranscoderException {
    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(svg))) {
      svg2Png(in,out);
    }
  }

  private static void svg2Png(InputStream insvg,OutputStream outpng) throws TranscoderException {
    Transcoder transcoder = new PNGTranscoder();
    TranscoderInput input = new TranscoderInput(insvg);
    TranscoderOutput output = new TranscoderOutput(outpng);
    transcoder.transcode(input, output);
  }


  public static void svg2Png(InputStream insvg, String filePath) throws IOException, TranscoderException {
    try(FileOutputStream ou = new FileOutputStream(filePath)){
      svg2Png(insvg, ou);
    }
  }



}
