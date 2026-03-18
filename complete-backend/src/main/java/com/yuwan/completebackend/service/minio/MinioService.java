package com.yuwan.completebackend.service.minio;

import com.yuwan.completebackend.config.MinioConfig;
import com.yuwan.completebackend.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO服务类
 * 封装MinIO的上传/下载/删除/预签名URL等方法
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Slf4j
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 检查并创建存储桶
     */
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
                log.info("创建存储桶: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("检查/创建存储桶失败", e);
            throw new BusinessException("MinIO存储桶操作失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     *
     * @param file       文件对象
     * @param objectName 对象名称（存储路径）
     * @return 文件存储路径
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("文件上传成功: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectName, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件流
     *
     * @param inputStream 输入流
     * @param objectName  对象名称
     * @param contentType 内容类型
     * @param size        文件大小
     * @return 文件存储路径
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("文件流上传成功: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件流上传失败: {}", objectName, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预签名URL（用于预览/下载）
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 预签名URL
     */
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(expiry, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}", objectName, e);
            throw new BusinessException("获取文件访问链接失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预签名URL（默认15分钟有效期）
     *
     * @param objectName 对象名称
     * @return 预签名URL
     */
    public String getPresignedUrl(String objectName) {
        return getPresignedUrl(objectName, 15 * 60);
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return true-存在 false-不存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件信息
     *
     * @param objectName 对象名称
     * @return StatObjectResponse
     */
    public StatObjectResponse getFileInfo(String objectName) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", objectName, e);
            throw new BusinessException("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取存储桶名称
     *
     * @return 存储桶名称
     */
    public String getBucketName() {
        return minioConfig.getBucketName();
    }
}
