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
}

