package com.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.dto.message.MessageResponse;
import com.library.service.MessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
	
	@Autowired
	private MessageService messageService;
	
	@GetMapping("/me")
	public ApiResponse<List<MessageResponse>> getMyMessages(){
		return ApiResponse.success("查詢訊息成功", messageService.getMyMessages());
	}
	
	@GetMapping("/me/unread-count")
	public ApiResponse<Long> getMyUnreadCount(){
		return ApiResponse.success("查詢位讀訊息數成功", messageService.getMyUnreadCount());
	}
	
	@PatchMapping("/{messageId}/read")//部分更新資料，因為只是標記已讀
	public ApiResponse<Void> markAsRead(@PathVariable Long messageId){
		messageService.markAsRead(messageId);
		return ApiResponse.success("已標記為已讀");
	}
	
}
