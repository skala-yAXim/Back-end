package com.yaxim.report.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;
import com.yaxim.report.exception.JsonParseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 형태의 문자열을 Object(Map)으로 파싱합니다.
     * @param jsonString DB에 저장된 JSON 문자열
     * @return 파싱된 Object. 파싱 실패 시 예외 발생
     */
    public static Object parseStringToObject(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, Object.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱에 실패했습니다. Original String: {}", jsonString, e);
            throw new JsonParseException();
        }
    }
}
