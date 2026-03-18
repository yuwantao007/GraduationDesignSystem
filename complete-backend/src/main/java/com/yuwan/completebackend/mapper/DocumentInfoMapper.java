package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.DocumentInfo;
import com.yuwan.completebackend.model.vo.DocumentInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档信息Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Mapper
public interface DocumentInfoMapper extends BaseMapper<DocumentInfo> {

    /**
     * 查询某学生的文档列表（带关联信息）
     *
     * @param studentId    学生ID
     * @param documentType 文档类型（可选）
     * @param latestOnly   是否只查最新版本
     * @return 文档列表
     */
    List<DocumentInfoVO> selectByStudentId(
            @Param("studentId") String studentId,
            @Param("documentType") Integer documentType,
            @Param("latestOnly") Boolean latestOnly
    );

    /**
     * 查询某课题的文档列表
     *
     * @param topicId      课题ID
     * @param documentType 文档类型（可选）
     * @return 文档列表
     */
    List<DocumentInfoVO> selectByTopicId(
            @Param("topicId") String topicId,
            @Param("documentType") Integer documentType
    );

    /**
     * 获取文档详情（带关联信息）
     *
     * @param documentId 文档ID
     * @return 文档详情
     */
    DocumentInfoVO selectDocumentDetail(@Param("documentId") String documentId);

    /**
     * 获取同类型文档的最大版本号
     *
     * @param studentId    学生ID
     * @param documentType 文档类型
     * @return 最大版本号
     */
    Integer selectMaxVersion(
            @Param("studentId") String studentId,
            @Param("documentType") Integer documentType
    );

    /**
     * 更新同类型文档的最新版本标识为非最新
     *
     * @param studentId    学生ID
     * @param documentType 文档类型
     * @param excludeDocId 排除的文档ID（当前上传的文档）
     * @return 影响行数
     */
    int updateNotLatest(
            @Param("studentId") String studentId,
            @Param("documentType") Integer documentType,
            @Param("excludeDocId") String excludeDocId
    );
}
