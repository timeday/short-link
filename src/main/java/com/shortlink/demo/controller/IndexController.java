package com.shortlink.demo.controller;

import com.shortlink.demo.service.ShortLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Slf4j
@Controller
public class IndexController {

    @Autowired
    private ShortLinkService shortLinkService;

    @GetMapping("/")
    public String index() {
        return "redirect:https://www.xiaocaojidi.com";
    }

    /**
     * 实现短链接跳转服务
     * @param request
     * @param path
     * @return
     */
    @GetMapping("/{path}")
    public ResponseEntity<Object> target(HttpServletRequest request, @PathVariable String path) {
        String host = URI.create(request.getRequestURL().toString()).getHost();
        // 作域名保护，只允许下述两个域名生成短链接
        if (!host.equals("t.xcjd.com") && !host.equals("t.xg2.cn")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("请求地址不存在");
        }
        URI uri = URI.create("http://" + host + "/" + path);
        String targetLink = shortLinkService.getTargetLink(uri);
        if (targetLink == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("请求地址不存在");
        }
        // 向下传递查询参数
        String queryString = request.getQueryString();
        if (queryString != null) {
            if (targetLink.contains("?")) targetLink = targetLink + "&" + queryString;
            else targetLink = targetLink + "?" + queryString;
        }
        log.info("-----targetLink:" + targetLink);
        return this.link(targetLink);
    }

    /**
     * 重定向请求
     * 跳转第三方链接,请求重定向防止拦截jSssionId
     * @param target
     * @return
     */
    @GetMapping("/link")
    public ResponseEntity<Object> link(@RequestParam String target) {
        URI uri = URI.create(target);
        // 链接检查
        if (uri == null || !uri.isAbsolute() || uri.getHost() == null) {
            log.warn("非法链接：{}", target);
            return ResponseEntity.badRequest().body("目标链接不是一个合法的链接地址");
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }

}
