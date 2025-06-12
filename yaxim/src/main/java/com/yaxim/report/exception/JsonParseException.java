package com.yaxim.report.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class JsonParseException extends CustomException {
    public JsonParseException() {
        super(ErrorCode.JSON_PARSE_ERROR);
    }
}
