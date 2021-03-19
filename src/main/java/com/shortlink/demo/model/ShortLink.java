package com.shortlink.demo.model;

import lombok.Data;

import java.net.URI;
import java.time.LocalDateTime;


@Data
public class ShortLink {

    private Long id;
    private URI shortLink;              // 短链接地址
    private String targetLink;          // 目标链接地址
    private String queryString;         // 查询条件字符串
    private String remark;              // 备注信息
    private String qrcode;              // 二维码地址
    private String username;            // 用户名
    private LocalDateTime createTime;   // 创建时间

}
