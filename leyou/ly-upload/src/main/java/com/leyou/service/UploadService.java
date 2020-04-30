package com.leyou.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {


    @Autowired
    FastFileStorageClient storageClient;
    @Autowired
    private UploadProperties prop;

    public String upload(MultipartFile file) {
        try {
            String type = file.getContentType();
            // 1 校验文件类型
            if (!prop.getAllowTypes().contains(type)) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 2 校验图片内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(
                    file.getInputStream(), file.getSize(), extension, null);
            // 返回完整路径
            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            // 上传失败
            // 记录日志
            log.error("上传图片失败", e);
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}
