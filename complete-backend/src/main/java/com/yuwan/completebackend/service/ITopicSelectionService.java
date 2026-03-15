package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.ApplyTopicDTO;
import com.yuwan.completebackend.model.dto.AssignTeacherDTO;
import com.yuwan.completebackend.model.vo.SelectionForTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionForUnivTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionOverviewVO;
import com.yuwan.completebackend.model.vo.UnivTeacherPairingVO;
import com.yuwan.completebackend.model.vo.TeacherAssignmentVO;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;
import com.yuwan.completebackend.model.vo.UnselectedStudentVO;

import java.util.List;

/**
 * 课题选报服务接口
 * 提供学生选报、教师确认、企业负责人双选审核的全链路业务功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
public interface ITopicSelectionService {

    // ==================== 学生选报 ====================

    /**
     * 查询可选课题列表（终审通过的课题，分页）
     */
    PageResult<TopicForSelectionVO> getAvailableTopics(
            Integer topicCategory,
            String guidanceDirection,
            String topicTitle,
            String majorId,
            int pageNum,
            int pageSize
    );

    /**
     * 学生选报课题
     */
    TopicSelectionVO applyTopic(ApplyTopicDTO dto);

    /**
     * 删除选报记录（仅落选后可删除）
     */
    void deleteSelection(String selectionId);

    /**
     * 查询当前学生的选报记录列表
     */
    List<TopicSelectionVO> getMySelections();

    // ==================== 教师确认子模块 ====================

    /**
     * 企业教师查看选报了自己课题的学生列表
     *
     * @param selectionStatus 选报状态过滤（null=全部，0=待确认，1=中选，2=落选）
     * @return 选报记录列表
     */
    List<SelectionForTeacherVO> getSelectionsForTeacher(Integer selectionStatus);

    /**
     * 企业教师确认人选（将选报状态 0→1）
     *
     * @param selectionId 选报记录ID
     * @return 更新后的选报记录
     */
    SelectionForTeacherVO confirmSelection(String selectionId);

    /**
     * 企业教师拒绝人选（将选报状态 0→2）
     *
     * @param selectionId 选报记录ID
     * @return 更新后的选报记录
     */
    SelectionForTeacherVO rejectSelection(String selectionId);

    /**
     * 企业教师导出已确认学生信息（Excel字节流）
     *
     * @return Excel 文件字节数组
     */
    byte[] exportConfirmedStudents();

    // ==================== 双选审核子模块 ====================

    /**
     * 企业负责人查看双选结果概览（课题维度）
     *
     * @return 双选概览列表
     */
    List<SelectionOverviewVO> getSelectionOverview();

    /**
     * 企业负责人查看未选报任何课题的学生列表
     *
     * @return 未选报学生列表
     */
    List<UnselectedStudentVO> getUnselectedStudents();

    /**
     * 企业负责人导出全部选题信息（Excel字节流）
     *
     * @return Excel 文件字节数组
     */
    byte[] exportSelectionInfo();

    /**
     * 企业负责人为校外协同课题中选学生指派企业指导教师
     *
     * @param dto 指派参数
     * @return 指派记录
     */
    TeacherAssignmentVO assignTeacher(AssignTeacherDTO dto);

    /**
     * 企业负责人取消指派
     *
     * @param assignmentId 指派记录ID
     */
    void cancelAssignment(String assignmentId);

    /**
     * 企业负责人查看企业内指派列表
     *
     * @return 指派记录列表
     */
    List<TeacherAssignmentVO> getAssignmentList();

    // ==================== 高校教师查看选题 ====================

    /**
     * 高校教师查看配对企业教师名下学生的选报结果
     * <p>
     * 通过 teacher_relationship 找到当前高校教师所配对的企业教师，
     * 再展示这些企业教师所创建课题的全部学生选报情况。
     * </p>
     *
     * @param selectionStatus 选报状态过滤（null=全部，0=待确认，1=中选，2=落选）
     * @return 选报结果列表
     */
    List<SelectionForUnivTeacherVO> getSelectionsForUnivTeacher(Integer selectionStatus);

    /**
     * 高校教师导出指导学生选题结果（Excel 字节流）
     *
     * @return Excel 文件字节数组
     */
    byte[] exportSelectionsForUnivTeacher();

    /**
     * 查询高校教师的配对关系列表（与选报数据无关，配对存在即可查到）
     * <p>
     * 用于在选报数据为空时也能展示教师配对状态，
     * 避免因无选报数据被误判为"未配对"。
     * </p>
     *
     * @return 配对信息列表（含企业教师、企业、课题/选报统计）
     */
    List<UnivTeacherPairingVO> getUnivTeacherPairings();
}
