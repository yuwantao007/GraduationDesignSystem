package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 文档类型枚举
 * <p>
 * 定义文档的类型分类
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Getter
public enum DocumentType {

    /** 项目代码 */
    PROJECT_CODE(1, "项目代码"),

    /** 论文文档 */
    THESIS_DOCUMENT(2, "论文文档"),

    /** 开题报告 */
    OPENING_REPORT(3, "开题报告"),

    /** 中期检查表 */
    MIDTERM_CHECK(4, "中期检查表"),

    /** 教师批注文档 */
    TEACHER_ANNOTATION(5, "教师批注文档");

    private final Integer code;
    private final String description;

    DocumentType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code值查找枚举
     *
     * @param code 类型代码
     * @return 对应枚举，不存在时返回 null
     */
    public static DocumentType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DocumentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取类型描述
     *
     * @param code 类型代码
     * @return 类型描述，不存在时返回空字符串
     */
    public static String getDescription(Integer code) {
        DocumentType type = fromCode(code);
        return type != null ? type.description : "";
    }

    /**
     * 判断是否为有效的文档类型
     *
     * @param code 类型代码
     * @return true-有效 false-无效
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
