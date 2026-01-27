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
    public List<InheritorProfile> getRecentList(int limit) {
        return this.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getVerifyStatus, 1)
                .orderByDesc(InheritorProfile::getAuditTime)
                .orderByDesc(InheritorProfile::getId)
                .last("LIMIT " + limit));
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

        // 2. 如果指定了根节点，则筛选该节点所属的整个家族树 (Find Root -> Find All Descendants of Root)
        List<InheritorProfile> targetProfiles;
        if (rootId != null) {
            // Find ancestors to locate the Ultimate Root
            List<InheritorProfile> ancestors = findAncestors(rootId, allProfiles);
            if (!ancestors.isEmpty()) {
                InheritorProfile ultimateRoot = ancestors.get(ancestors.size() - 1);
                // Find all descendants of the Ultimate Root (this includes the root itself)
                targetProfiles = findDescendants(ultimateRoot.getId(), allProfiles);
            } else {
                targetProfiles = new ArrayList<>();
            }
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
            String name = "未知传承人";
            if (user != null) {
                if (user.getRealName() != null && !user.getRealName().isEmpty()) {
                    name = user.getRealName();
                } else if (user.getUsername() != null) {
                    name = user.getUsername();
                }
            }
            String avatar = (user != null && user.getAvatar() != null) ? user.getAvatar() : "";
            
            // 构建节点
            nodes.add(new GraphNodeDto(
                    String.valueOf(profile.getId()),
                    name,
                    avatar,
                    profile.getLevel(),
                    profile.getGenre(),
                    profile.getArtisticCareer(),
                    rootId != null && profile.getId().equals(rootId)
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
        Set<Long> visited = new HashSet<>();
        
        queue.add(root);
        visited.add(root.getId());

        while (!queue.isEmpty()) {
            InheritorProfile current = queue.poll();
            result.add(current);

            List<InheritorProfile> disciples = adjacency.get(current.getId());
            if (disciples != null) {
                for (InheritorProfile disciple : disciples) {
                    if (!visited.contains(disciple.getId())) {
                        visited.add(disciple.getId());
                        queue.add(disciple);
                    }
                }
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

    @Override
    public com.lqx.opera.common.dto.LineageTreeNodeDto getLineageTree(Long inheritorId) {
        // 1. Get all verified profiles
        List<InheritorProfile> all = this.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getVerifyStatus, 1));
        
        // 2. Build map for quick access
        Map<Long, InheritorProfile> profileMap = all.stream().collect(Collectors.toMap(InheritorProfile::getId, p -> p));
        
        // 3. Find target
        InheritorProfile target = profileMap.get(inheritorId);
        if (target == null) return null;
        
        // 4. Find root (ancestor)
        InheritorProfile root = target;
        Set<Long> visited = new HashSet<>();
        while (root.getMasterId() != null && profileMap.containsKey(root.getMasterId())) {
            if (visited.contains(root.getId())) break; 
            visited.add(root.getId());
            root = profileMap.get(root.getMasterId());
        }
        
        // 5. Get User info map
        List<Long> userIds = all.stream().map(InheritorProfile::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = sysUserService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getUserId, u -> u));
        }
        
        return buildTreeNode(root, all, userMap, inheritorId);
    }

    private com.lqx.opera.common.dto.LineageTreeNodeDto buildTreeNode(InheritorProfile current, List<InheritorProfile> all, Map<Long, SysUser> userMap, Long targetId) {
        com.lqx.opera.common.dto.LineageTreeNodeDto node = new com.lqx.opera.common.dto.LineageTreeNodeDto();
        node.setId(current.getId());
        
        SysUser u = userMap.get(current.getUserId());
        String name = "未知传承人";
        if (u != null) {
            if (u.getRealName() != null && !u.getRealName().isEmpty()) {
                name = u.getRealName();
            } else if (u.getUsername() != null) {
                name = u.getUsername();
            }
        }
        node.setName(name);
        node.setLevel(current.getLevel());
        node.setIsTarget(current.getId().equals(targetId));
        
        List<InheritorProfile> children = all.stream()
                .filter(p -> current.getId().equals(p.getMasterId()))
                .collect(Collectors.toList());
                
        if (!children.isEmpty()) {
            node.setChildren(children.stream()
                    .map(child -> buildTreeNode(child, all, userMap, targetId))
                    .collect(Collectors.toList()));
        }
        return node;
    }

    @Override
    public List<InheritorProfile> searchVerifiedMasters(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<SysUser> users = sysUserService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .like(SysUser::getRealName, query)
                .or()
                .like(SysUser::getUsername, query));
        
        if (users.isEmpty()) return Collections.emptyList();
        
        List<Long> userIds = users.stream().map(SysUser::getUserId).collect(Collectors.toList());
        
        return this.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getVerifyStatus, 1)
                .in(InheritorProfile::getUserId, userIds));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void auditInheritor(Long id, Integer status, String remark) {
        InheritorProfile profile = this.getById(id);
        if (profile == null) {
            throw new RuntimeException("档案不存在");
        }
        
        profile.setVerifyStatus(status);
        profile.setAuditRemark(remark);
        profile.setAuditTime(java.time.LocalDateTime.now());
        
        this.updateById(profile);

        // 如果审核通过，更新用户角色
        if (status == 1) {
            SysUser user = sysUserService.getById(profile.getUserId());
            if (user != null) {
                user.setRole(1); // 1-Inheritor
                sysUserService.updateById(user);
            }
        }
        
        // Notification logic would go here
        // e.g., messageService.send(profile.getUserId(), "Your application has been " + (status == 1 ? "approved" : "rejected"));
    }
}

