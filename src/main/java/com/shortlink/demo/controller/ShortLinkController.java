package com.shortlink.demo.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.shortlink.demo.model.ShortLink;
import com.shortlink.demo.service.ShortLinkService;
import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/short")
public class ShortLinkController {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private Hashids hashids;
    @Autowired
    private ShortLinkService shortLinkService;

    // 系统临时目录
    private String tmp = System.getProperty("java.io.tmpdir");

    // 二维码图片规格
    private final String format = "png";

    /**
     * 短链接记录表单
     * @return
     */
    @GetMapping({ "/form" })
    public String form(HttpSession session) {
        String token = UUID.randomUUID().toString();
        session.setAttribute("token", token);
        return "short";
    }

    @PostMapping("/create")
    public String show(HttpSession session,
                       HttpServletRequest request,
                       ModelMap model,
                       @RequestParam String token,
                       @ModelAttribute ShortLink link) throws IOException {
        // Token检查
        if (token == null || !token.equals(session.getAttribute("token"))) {
            model.addAttribute("error_message", "页面已过期，请重试");
            return "short_show";
        }

        // 参数检查
        if (link == null || link.getTargetLink() == null || link.getRemark() == null || link.getUsername() == null) {
            model.addAttribute("error_message", "必填参数未填写，请重试");
            return "short_show";
        }

        // 检查目标链接地址是否合法
        URI targetUri = URI.create(link.getTargetLink());
        if (targetUri == null || !targetUri.isAbsolute() || targetUri.getHost() == null) {
            log.warn("非法链接：{}", link.getTargetLink());
            model.addAttribute("error_message", "目标链接地址不合法，请重试");
            return "short_show";
        }

        String host = URI.create(request.getRequestURL().toString()).getHost();
        // 作域名保护，只允许下述两个域名生成短链接
        // 防止第三方拦截做短链接重定向
        if (!host.equals("t.xcjd.com") && !host.equals("t.xg2.cn")) {
            return "redirect:http://t.xcjd.com/short/form";
        }

        // 生成短链接逻辑
        long number = redisTemplate.opsForValue().increment("xc:short:numbers", 1L);
        URI shortLink = URI.create("http://" + host + "/" + hashids.encodeHex(String.valueOf(number)));
        link.setShortLink(shortLink);

        // 生成二维码
        String qrcode = qrcode(shortLink.toString());
        link.setQrcode(qrcode);

        // 持久化，建立短链接与目标链接映射关系(缓存)
        link.setCreateTime(LocalDateTime.now());
        long id = shortLinkService.save(link);

        log.info("生成短链接：{}，目标链接：{}", link.getShortLink(), link.getTargetLink());

        // 展示结果
        return "redirect:/short/" + id;
    }

    @GetMapping("/{id}")
    public String show(ModelMap model, @PathVariable long id) {
        ShortLink link = shortLinkService.get(id);
        if (link == null) {
            model.addAttribute("error_message", "查询短链接信息不存在");
            return "short_show";
        }
        model.addAttribute("shortLink", link.getShortLink());
        model.addAttribute("targetLink", link.getTargetLink());
        model.addAttribute("qrcode", link.getQrcode());
        return "short_show";
    }


    /**
     * 生成二维码逻辑
     * @param link
     * @return
     */
    private String qrcode(String link) throws IOException {
        if (link == null) return null;
        // 生成二维码
        Path path = generate(ErrorCorrectionLevel.H, 1024, 1024, 2, link);
        log.info("生成二维码临时文件地址：{}", path.toUri().toString());

        //上传ftp服务器 TODO


        return path.toString();
    }

    /**
     * 生成二维码
     * @param level
     * @param width
     * @param height
     * @param margin
     * @param link
     * @return
     */
    private Path generate(ErrorCorrectionLevel level, int width, int height, int margin, String link) throws IOException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置容错率，默认L，取值范围：L(7%)、M(15%)、Q(25%)、H(%30)
        hints.put(EncodeHintType.ERROR_CORRECTION, level);
        // 设置白色边框值(margin)，设置为0时，没有边框
        hints.put(EncodeHintType.MARGIN, margin);

        Path path = Paths.get(new File(tmp, "/" + UUID.randomUUID().toString() + "." + format).getAbsolutePath());
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            // 生成一张黑色背景图
            ImageIO.write(image, format, path.toFile());
            // 生成二维码图案
            BitMatrix bitMatrix = new QRCodeWriter()
                    .encode(link,
                            BarcodeFormat.QR_CODE,
                            width,
                            height,
                            hints);
            MatrixToImageWriter.writeToPath(bitMatrix, format, path);
        } catch (Exception e) {
            log.error("二维码生成出错", e);
        }

        return path;
    }

}
