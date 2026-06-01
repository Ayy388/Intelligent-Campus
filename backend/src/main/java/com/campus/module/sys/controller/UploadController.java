package com.campus.module.sys.controller;

import com.campus.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.avatar-dir:uploads/avatars}")
    private String avatarDir;

    @Value("${upload.image-dir:uploads/images}")
    private String imageDir;

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, avatarDir, "avatars");
    }

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, imageDir, "images");
    }

    private Result<String> uploadFile(MultipartFile file, String dir, String subPath) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + ext;

        try {
            Path uploadPath = Paths.get(dir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            file.transferTo(uploadPath.resolve(fileName));
            String url = "/uploads/" + subPath + "/" + fileName;
            return Result.ok(url);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
