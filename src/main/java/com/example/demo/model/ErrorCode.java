package com.example.demo.model;

public enum ErrorCode {
    SUCCESS("0000","Success"),
    USER_NOT_EXIST("0010","User not exist"),
    USER_ALREADY_EXIST("0011","User already exist"),
    TRANSFER_AMOUNT_ERROR("0020","Amount can not be negative value"),
    NOT_ALLOW_CANCEL("0080","Not allow cancel"),
    ERROR("9999","Operation fail");
    private String code;
    private String message;
    ErrorCode(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
