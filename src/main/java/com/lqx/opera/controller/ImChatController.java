package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.entity.ImChatMessage;
import com.lqx.opera.websocket.ImChatMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/im/chat")
public class ImChatController {

    private final ImChatMessageService imChatMessageService;

    public ImChatController(ImChatMessageService imChatMessageService) {
        this.imChatMessageService = imChatMessageService;
    }

    /**
     * 撤回消息
     * POST /api/im/chat/recall
     */
    @PostMapping("/recall")
    public Result<Boolean> recallMessage(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        try {
            Long messageId = Long.valueOf(body.get("messageId").toString());
            imChatMessageService.recallMessage(userId, messageId);
            return Result.success(true);
        } catch (NumberFormatException e) {
            return Result.fail("参数错误");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取历史消息
     * GET /api/im/chat/history
     */
    @GetMapping("/history")
    public Result<List<ImChatMessage>> getHistory(
            @RequestAttribute Long userId,
            @RequestParam Long friendId,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(imChatMessageService.getHistoryMessages(userId, friendId, size));
    }
}
