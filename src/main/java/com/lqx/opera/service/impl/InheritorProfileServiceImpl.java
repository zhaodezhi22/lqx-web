package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.common.dto.GraphLinkDto;
import com.lqx.opera.common.dto.GraphNodeDto;
import com.lqx.opera.common.dto.GraphResultDto;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.InheritorProfileMapper;
import com.lqx.opera.service.InheritorProfileService;
import com.lqx.opera.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InheritorProfileServiceImpl extends ServiceImpl<InheritorProfileMapper, InheritorProfile> implements InheritorProfileService {

    private final SysUserService sysUserService;

    public InheritorProfileServiceImpl(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public List<InheritorProfile> getListSortedByLevel() {
        return this.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InheritorProfile>()
                .eq("verify_status", 1)
                .last("ORDER BY CASE level WHEN '国家级' THEN 1 WHEN '省级' THEN 2 WHEN '市级' THEN 3 ELSE 4 END ASC, id DESC"));
    }

    @Override
    public GraphResultDto getLineageGraph(Long rootId) {
        // 1. 查询所有审核通过的传承人
        List<InheritorProfile> allProfiles = this.lambdaQuery()
                .eq(InheritorProfile::getVerifyStatus, 1)
                .list();

        if (allProfiles == null || allProfiles.isEmpty()) {
            return new GraphResultDto(Collections.emptyList(), Collections.emptyList());
        }

        // 2. 如果指定了根节点，则筛选该节点及其所有后代 + 祖先
        List<InheritorProfile> targetProfiles;
        if (rootId != null) {
            List<InheritorProfile> descendants = findDescendants(rootId, allProfiles);
            List<InheritorProfile> ancestors = findAncestors(rootId, allProfiles);
            
            Set<InheritorProfile> merged = new HashSet<>();
            merged.addAll(descendants);
            merged.addAll(ancestors);
            targetProfiles = new ArrayList<>(merged);
        } else {
            targetProfiles = allProfiles;
        }

        if (targetProfiles.isEmpty()) {
            return new GraphResultDto(Collections.emptyList(), Collections.emptyList());
        }

        // 3. 获取用户信息 (用于填充姓名和头像)
        Set<Long> userIds = targetProfiles.stream()
                .map(InheritorProfile::getUserId)
                .collect(Collectors.toSet());
        
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserService.listByIds(userIds);
            if (users != null) {
                userMap = users.stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
            }
        }

        // 4. 构建节点 (Nodes) 和 连线 (Links)
        List<GraphNodeDto> nodes = new ArrayList<>();
        List<GraphLinkDto> links = new ArrayList<>();
        Set<Long> targetIds = targetProfiles.stream().map(InheritorProfile::getId).collect(Collectors.toSet());

        for (InheritorProfile profile : targetProfiles) {
            SysUser user = userMap.get(profile.getUserId());
            String name = (user != null && user.getRealName() != null) ? user.getRealName() : "未知传承人";
            String avatar = (user != null && user.getAvatar() != null) ? user.getAvatar() : "";
            
            // 构建节点
            nodes.add(new GraphNodeDto(
                    String.valueOf(profile.getId()),
                    name,
                    avatar,
                    profile.getLevel(),
                    profile.getGenre(),
                    profile.getArtisticCareer()
            ));

            // 构建连线 (如果存在师父，且师父也在当前展示的图谱中)
            // 注意：如果 viewing 'All', 只要 masterId 存在，通常 master 也在 allProfiles 中 (除非 master 未通过审核)
            if (profile.getMasterId() != null) {
                // 只有当师父也在目标集合中时才创建连线，保证图的完整性
                if (targetIds.contains(profile.getMasterId())) {
                    links.add(new GraphLinkDto(
                            String.valueOf(profile.getMasterId()), // Source: Master
                            String.valueOf(profile.getId())        // Target: Disciple
                    ));
                }
            }
        }

        return new GraphResultDto(nodes, links);
    }

    /**
     * 递归查找指定节点及其所有后代
     */
    private List<InheritorProfile> findDescendants(Long rootId, List<InheritorProfile> allProfiles) {
        // 构建邻接表: MasterId -> List<Disciple>
        Map<Long, List<InheritorProfile>> adjacency = allProfiles.stream()
                .filter(p -> p.getMasterId() != null)
                .collect(Collectors.groupingBy(InheritorProfile::getMasterId));

        List<InheritorProfile> result = new ArrayList<>();
        
        // 查找根节点
        InheritorProfile root = allProfiles.stream()
                .filter(p -> p.getId().equals(rootId))
                .findFirst()
                .orElse(null);
        
        if (root == null) {
            return result;
        }

        // BFS 遍历收集后代
        Queue<InheritorProfile> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            InheritorProfile current = queue.poll();
            result.add(current);

            List<InheritorProfile> disciples = adjacency.get(current.getId());
            if (disciples != null) {
                queue.addAll(disciples);
            }
        }

        return result;
    }

    private List<InheritorProfile> findAncestors(Long rootId, List<InheritorProfile> allProfiles) {
        Map<Long, InheritorProfile> profileMap = allProfiles.stream()
                .collect(Collectors.toMap(InheritorProfile::getId, p -> p));

        List<InheritorProfile> result = new ArrayList<>();
        InheritorProfile current = profileMap.get(rootId);
        
        // 向上追溯
        while (current != null) {
            result.add(current);
            if (current.getMasterId() == null) {
                break;
            }
            // 防止环状依赖导致的死循环
            Long masterId = current.getMasterId();
            // 如果已经包含该师父(环)，则停止
            if (result.stream().anyMatch(p -> p.getId().equals(masterId))) {
                break;
            }
            current = profileMap.get(masterId);
        }
        return result;
    }
}

