package com.library.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
	
	public static String generate(String prefix) {
		String time=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String random=UUID.randomUUID().toString().replace("-", "").substring(0, 4);
		return prefix+time+random;
	}

	public static String generateUserId() {
        return generate("U");
    }

    public static String generateBookId() {
        return generate("BK");
    }

    public static String generateBookCopyId() {
        return generate("CP");
    }
	
}