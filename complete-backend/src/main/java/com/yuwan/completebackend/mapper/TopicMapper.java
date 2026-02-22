package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.vo.TopicListVO;
import com.yuwan.completebackend.model.vo.TopicQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 课题信息Mapper接口
 * <p>单表 CRUD 优先使用 MyBatis-Plus BaseMapper + QueryWrapper，
 * 多表联查使用手写 SQL</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    /**
     * 分页查询课题列表（包含企业名称、创建人姓名）
     *
     * @param page    分页参数
     * @param queryVO 查询条件
     * @return 分页结果
     */
    IPage<TopicListVO> selectTopicListPage(Page<TopicListVO> page, @Param("query") TopicQueryVO queryVO);

    /**
     * 统计指定企业教师通过终审的课题数量
     *
     * @param creatorId 创建人ID（企业教师ID）
     * @return 通过终审的课题数量
     */
    @Select("SELECT COUNT(*) FROM topic_info WHERE creator_id = #{creatorId} AND review_status = 6 AND deleted = 0")
    int countPassedTopicsByCreator(@Param("creatorId") String creatorId);

    /**
     * 检查课题名称是否已存在
     *
     * @param topicTitle 课题名称
     * @param excludeId  排除的课题ID（用于更新时排除自身）
     * @return 存在数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM topic_info WHERE topic_title = #{topicTitle} AND deleted = 0 " +
            "<if test='excludeId != null'> AND topic_id != #{excludeId}</if>" +
            "</script>")
    int countByTopicTitle(@Param("topicTitle") String topicTitle, @Param("excludeId") String excludeId);
}
