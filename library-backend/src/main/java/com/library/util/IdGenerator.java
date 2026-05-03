package com.library.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
	//方法A
	public static String generate(String prefix) {
		String time=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String random=UUID.randomUUID().toString().replace("-", "").substring(0, 3);
		return prefix+time+random;
	}
	//方法B?麻煩的地方是要get資料庫list.size
	public String generateUserId(long count) {
        return String.format("U%08d", count+1);
    }
    public String generateBookId(long count) {
        return String.format("BK%08d", count+1);
    }
    public String generateBookCopyId(long count) {
        return String.format("CP%08d", count+1);
    }
	
}