package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.service.PerformanceEventService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/event")
public class AdminEventController {

    private final PerformanceEventService performanceEventService;

    public AdminEventController(PerformanceEventService performanceEventService) {
        this.performanceEventService = performanceEventService;
    }

    @PutMapping("/audit")
    @RequireRole({2, 3})
    public Result<Boolean> audit(@RequestBody AuditRequest req) {
        PerformanceEvent event = performanceEventService.getById(req.getId());
        if (event == null) return Result.fail("演出不存在");
        
        event.setStatus(req.getStatus()); // 1-Pass, 2-Reject
        return performanceEventService.updateById(event) ? Result.success(true) : Result.fail("审核失败");
    }

    @PostMapping("/offline/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> offline(@PathVariable Long id) {
        PerformanceEvent event = performanceEventService.getById(id);
        if (event == null) return Result.fail("演出不存在");

        event.setStatus(3); // 3-Offline
        return performanceEventService.updateById(event) ? Result.success(true) : Result.fail("下架失败");
    }

    @lombok.Data
    public static class AuditRequest {
        private Long id;
        private Integer status;
        private String remark;
    }

    /**
     * 更新座位布局
     * @param id 演出ID
     * @param body 包含 seatLayoutJson 的 Map 或直接传 String/List
     *             这里为了灵活，我们接收 Map，期望 key 为 "seatLayoutJson" 或直接接收 List
     *             User requirement: "将最终的 JSON 对象通过 PUT ... 更新"
     *             Usually frontend sends { seatLayoutJson: [...] } or just [...]
     *             Let's support a wrapper object { "seatLayoutJson": ... }
     */
    @PutMapping("/seats/{id}")
    @RequireRole({2, 3}) // Admin/Auditor
    public Result<Boolean> updateSeats(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PerformanceEvent event = performanceEventService.getById(id);
        if (event == null) {
            return Result.fail(404, "演出不存在");
        }

        Object jsonObj = body.get("seatLayoutJson");
        if (jsonObj == null) {
            // Fallback: maybe the body itself is the list? 
            // But @RequestBody Map implies a JSON object. 
            // If user sends array, we should use List<Object>.
            // Let's assume frontend sends { seatLayoutJson: [...] }
            return Result.fail("参数 seatLayoutJson 缺失");
        }

        // Convert to String if it's not
        String jsonStr;
        if (jsonObj instanceof String) {
            jsonStr = (String) jsonObj;
        } else {
            // It might be a List or Map, convert back to JSON string
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                jsonStr = mapper.writeValueAsString(jsonObj);
            } catch (Exception e) {
                return Result.fail("JSON 格式错误");
            }
        }

        event.setSeatLayoutJson(jsonStr);
        // We might also want to update totalSeats if it changes, but let's stick to just saving the JSON for now.
        // Or calculate it? 
        // Let's keep it simple.
        
        boolean success = performanceEventService.updateById(event);
        return success ? Result.success(true) : Result.fail("更新失败");
    }
}
