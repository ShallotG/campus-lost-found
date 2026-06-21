package com.campus.lostfound.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;

    // 无数据成功响应
    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null, System.currentTimeMillis());
    }

    // 有数据成功响应
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }

    // 有数据 + 自定义消息
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }

    // 无数据 + 自定义消息 (显式返回 Result<Void>，避免泛型推断为 Result<String>)
    public static Result<Void> ok(String message) {
        return new Result<>(200, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null, System.currentTimeMillis());
    }
}
