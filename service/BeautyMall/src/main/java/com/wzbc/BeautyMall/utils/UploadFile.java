package com.wzbc.BeautyMall.utils;

import com.wzbc.BeautyMall.Exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class UploadFile {

    public static String Upload_File(MultipartFile multipartFile, String targetPath, String fileName) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException("文件不存在");
        }
        try {
            File targetFolder = new File(targetPath);
            // 如果目录不存在，创建目录
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            // 拼接完整文件路径
            File targetFile = new File(targetFolder, fileName);
            // 保存文件
            multipartFile.transferTo(targetFile);
            return targetFile.getPath();

        } catch (Exception e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }
}
