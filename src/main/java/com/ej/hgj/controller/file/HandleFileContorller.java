package com.ej.hgj.controller.file;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 通用文件上传,文件下载,文件预览
 *
 * @author tty
 * @version 1.0 2020-08-28 16:37
 */
@Controller
public class HandleFileContorller {

    @RequestMapping(value={"/image/query","/queryImgUrl"})
    public void  checkImage(HttpServletResponse response, String fileName, String packName,String customize) {
        response.setContentType("image/jpeg");

        InputStream in  = HandleFileContorller.class.getResourceAsStream("/images/0.png");
        try {
            OutputStream os = response.getOutputStream(); // 创建输出流
            byte[] b = new byte[1024];
            while (in.read(b) != -1) {
                os.write(b);
            }
            in.close();
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}