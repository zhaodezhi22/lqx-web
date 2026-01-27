package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.GraphResultDto;
import com.lqx.opera.entity.InheritorProfile;

public interface InheritorProfileService extends IService<InheritorProfile> {
    /**
     * 获取传承人谱系图数据
     * @param rootId 可选，根节点ID。如果不传则返回全量图
     * @return 节点和连线数据
     */
    GraphResultDto getLineageGraph(Long rootId);

    /**
     * 获取指定传承人的谱系树（以该传承人为焦点，向上找根，向下找所有子孙）
     * @param inheritorId 焦点传承人ID
     * @return 树形结构
     */
    com.lqx.opera.common.dto.LineageTreeNodeDto getLineageTree(Long inheritorId);

    /**
     * 获取按等级排序的传承人列表
     * 排序规则：国家级 > 省级 > 市级 > 其他
     * @return 排序后的列表
     */
    java.util.List<InheritorProfile> getListSortedByLevel();

    /**
     * 获取最近认证的传承人列表
     * @param limit 数量限制
     * @return 按时间倒序的列表
     */
    java.util.List<InheritorProfile> getRecentList(int limit);

    /**
     * 搜索审核通过的传承人（用于表单选择师父）
     * @param query 名字关键词
     * @return 列表
     */
    java.util.List<InheritorProfile> searchVerifiedMasters(String query);

    /**
     * 审核传承人
     * @param id 档案ID
     * @param status 1-Pass, 2-Reject
     * @param remark 审核意见
     */
    void auditInheritor(Long id, Integer status, String remark);
}

