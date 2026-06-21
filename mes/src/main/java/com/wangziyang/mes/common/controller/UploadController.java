package com.wangziyang.mes.common.controller;

import com.wangziyang.mes.common.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Controller
@RequestMapping("/upload")
public class UploadController {

    /**
     * 上传物料图片
     */
    @PostMapping("/material-img")
    @ResponseBody
    public Result uploadMaterialImg(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return doUpload(file, request, "/upload/material/");
    }

    /**
     * 上传工艺附图
     */
    @PostMapping("/process-img")
    @ResponseBody
    public Result uploadProcessImg(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return doUpload(file, request, "/upload/process/");
    }

    /**
     * 上传技术文档
     */
    @PostMapping("/tech-doc")
    @ResponseBody
    public Result uploadTechDoc(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.failure("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.failure("文件名异常");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        String realPath = request.getServletContext().getRealPath("/upload/tech/");
        File destDir = new File(realPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File destFile = new File(destDir, newFileName);
        try {
            file.transferTo(destFile);
            Map<String, String> result = new HashMap<>();
            result.put("name", originalFilename);
            result.put("url", "/upload/tech/" + newFileName);
            return Result.success(result);
        } catch (IOException e) {
            return Result.failure("上传失败：" + e.getMessage());
        }
    }

    private Result doUpload(MultipartFile file, HttpServletRequest request, String relativePath) {
        if (file.isEmpty()) {
            return Result.failure("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.failure("文件名异常");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        String realPath = request.getServletContext().getRealPath(relativePath);
        File destDir = new File(realPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File destFile = new File(destDir, newFileName);
        try {
            file.transferTo(destFile);
            return Result.success(relativePath + newFileName);
        } catch (IOException e) {
            return Result.failure("上传失败：" + e.getMessage());
        }
    }
}
