package com.shortlink.demo.service;

import com.shortlink.demo.dao.ShortLinkDao;
import com.shortlink.demo.model.ShortLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;

import static org.springframework.util.Assert.notNull;

@Service
public class ShortLinkService {

    @Autowired
    private ShortLinkDao shortLinkDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 创建短链接
     * @param dto
     * @return
     */
    public Long save(ShortLink dto) {
        notNull(dto, "dto对象不能为空");
        notNull(dto.getShortLink(), "短链接参数不能为空");
        notNull(dto.getTargetLink(), "目标链接参数不能为空");

        // 持久化
        Long id = shortLinkDao.save(dto);

        // 在缓存中映射
        redisTemplate.opsForValue().set("base:short:" + dto.getShortLink().getPath(), dto.getTargetLink());

        return id;
    }

    /**
     * 根据短链接查询目标链接
     * @param shortLink
     * @return
     */
    public String getTargetLink(URI shortLink) {
        if (shortLink == null) return null;
        String targetLink = redisTemplate.opsForValue().get("base:short:" + shortLink.getPath());
        if (targetLink != null) {
            if(targetLink.indexOf("zhihuishu") >= 0) {
                targetLink = targetLink.replace("http://", "https://");
            }
            return targetLink;
        }
        targetLink = shortLinkDao.getTargetLink(shortLink.toString());
        if (targetLink != null) {
            redisTemplate.opsForValue().set("base:short:" + shortLink.getPath(), targetLink);
            if(targetLink.indexOf("zhihuishu") >= 0) {
                targetLink = targetLink.replace("http://", "https://");
            }
        }
        return targetLink;
    }

    /**
     * 主键查询短链接信息
     * @param id
     * @return
     */
    public ShortLink get(long id) {
        return shortLinkDao.get(id);
    }
}
