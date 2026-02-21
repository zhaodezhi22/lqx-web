package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.CommunityComment;
import com.lqx.opera.mapper.CommunityCommentMapper;
import com.lqx.opera.service.CommunityCommentService;
import org.springframework.stereotype.Service;

@Service
public class CommunityCommentServiceImpl extends ServiceImpl<CommunityCommentMapper, CommunityComment> implements CommunityCommentService {
}
