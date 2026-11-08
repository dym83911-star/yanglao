package com.elderlycare.controller;

import com.elderlycare.common.BusinessException;
import com.elderlycare.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 图片上传：保存到本地 uploads 目录，返回可直接访问的相对地址
 */
@RestController
@RequestMapping("/api/upload")
public class FileController {

    private static final long MAX_SIZE = 5 * 1024 * 1024L;
    private static final List<String> ALLOWED = Arrays.asList("jpg", "png", "gif", "webp");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public Result<Object> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "图片不能超过 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "只支持图片文件");
        }
        String ext = contentType.substring("image/".length()).toLowerCase()
                .replace("jpeg", "jpg").replace("svg+xml", "svg");
        if (!ALLOWED.contains(ext)) {
            throw new BusinessException(400, "仅支持 jpg / png / gif / webp 格式");
        }

        File dir = new File(uploadDir).getAbsoluteFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(500, "上传目录创建失败");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            file.transferTo(new File(dir, fileName));
        } catch (Exception e) {
            throw new BusinessException(500, "图片保存失败");
        }

        return Result.success(Collections.singletonMap("url", "/uploads/" + fileName));
    }
}
