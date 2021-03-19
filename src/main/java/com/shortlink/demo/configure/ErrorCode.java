package com.shortlink.demo.configure;

public enum ErrorCode {

    /** 无效参数列表 */
    INVALID_ARGUMENTS(-1 ,"无效参数列表") ,
    INVALID_CREDENTIAL(-2 ,"无效凭证") ,
    UNAUTHORIZED(-3 ,"未授权操作") ,
    TOO_MANY_REQUEST(-4 ,"单位时间内请求次数过多") ,

    ;

    private int value ;
    private String message ;

    /**
     * 使用错误码及错误消息构造错误状态码
     * @param value
     * @param message
     */
    private ErrorCode(int value, String message) {
        this.value = value ;
        this.message = message ;
    }

    public int getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

}

