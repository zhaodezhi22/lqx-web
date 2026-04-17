package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.ImFriendDto;
import com.lqx.opera.common.dto.ImFriendRequestDto;
import com.lqx.opera.service.ImFriendRequestService;
import com.lqx.opera.service.ImFriendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/im/friend")
public class ImFriendController {

    private final ImFriendService imFriendService;
    private final ImFriendRequestService imFriendRequestService;

    public ImFriendController(ImFriendService imFriendService, ImFriendRequestService imFriendRequestService) {
        this.imFriendService = imFriendService;
        this.imFriendRequestService = imFriendRequestService;
    }

    /**
     * 发送好友申请
     * POST /api/im/friend/request
     */
    @PostMapping("/request")
    public Result<Boolean> sendRequest(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        try {
            Long friendId = Long.valueOf(body.get("friendId").toString());
            String reason = (String) body.get("reason");
            imFriendRequestService.sendRequest(userId, friendId, reason);
            return Result.success(true);
        } catch (NumberFormatException e) {
            return Result.fail("参数错误");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取待处理的好友申请
     * GET /api/im/friend/request/pending
     */
    @GetMapping("/request/pending")
    public Result<List<ImFriendRequestDto>> listPendingRequests(@RequestAttribute Long userId) {
        return Result.success(imFriendRequestService.listPendingRequests(userId));
    }

    /**
     * 同意好友申请
     * POST /api/im/friend/accept
     */
    @PostMapping("/accept")
    public Result<Boolean> acceptRequest(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        try {
            Long requestId = Long.valueOf(body.get("requestId").toString());
            imFriendRequestService.acceptRequest(userId, requestId);
            return Result.success(true);
        } catch (NumberFormatException e) {
            return Result.fail("参数错误");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 拒绝好友申请
     * POST /api/im/friend/reject
     */
    @PostMapping("/reject")
    public Result<Boolean> rejectRequest(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        try {
            Long requestId = Long.valueOf(body.get("requestId").toString());
            imFriendRequestService.rejectRequest(userId, requestId);
            return Result.success(true);
        } catch (NumberFormatException e) {
            return Result.fail("参数错误");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取好友列表
     * GET /api/im/friend/list
     */
    @GetMapping("/list")
    public Result<List<ImFriendDto>> listFriends(@RequestAttribute Long userId) {
        return Result.success(imFriendService.listFriends(userId));
    }

    /**
     * 删除好友
     * DELETE /api/im/friend/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public Result<Boolean> deleteFriend(@RequestAttribute Long userId, @PathVariable Long friendId) {
        imFriendService.deleteFriend(userId, friendId);
        return Result.success(true);
    }
}
