package com.shortlink.demo.dao;

import com.shortlink.demo.model.ShortLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.springframework.util.Assert.notNull;


@Slf4j
@Repository
public class ShortLinkDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存短链接数据
     * @param model
     * @return 返回主键
     */
    public Long save(ShortLink model) {
        notNull(model, "model对象不能为空");
        notNull(model.getShortLink(), "短链接参数不能为空");
        notNull(model.getTargetLink(), "目标链接参数不能为空");
        if (model.getCreateTime() == null) model.setCreateTime(LocalDateTime.now());

        KeyHolder holder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(psc -> {
            StringBuilder builder0 = new StringBuilder("INSERT INTO TBL_SHORT_LINK (SHORT_LINK, TARGET_LINK, CREATE_TIME");
            StringBuilder builder2 = new StringBuilder(" VALUES (?, ?, ?");
            if (model.getQueryString() != null) {
                builder0.append(", QUERY_STRING");
                builder2.append(", ?");
            }
            if (model.getRemark() != null) {
                builder0.append(", REMARK");
                builder2.append(", ?");
            }
            if (model.getQrcode() != null) {
                builder0.append(", QRCODE");
                builder2.append(", ?");
            }
            if (model.getUsername() != null) {
                builder0.append(", USERNAME");
                builder2.append(", ?");
            }
            builder0.append(")");
            builder2.append(")");
            String sql = builder0.append(builder2).toString();
            log.info("SQL = {}", sql);

            PreparedStatement statement = psc.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            int index = 0;
            statement.setString(++ index, model.getShortLink().toString());
            statement.setString(++ index, model.getTargetLink());
            statement.setTimestamp(++ index, Timestamp.valueOf(model.getCreateTime()));
            if (model.getQueryString() != null) statement.setString(++ index, model.getQueryString());
            if (model.getRemark() != null) statement.setString(++ index, model.getRemark());
            if (model.getQrcode() != null) statement.setString(++ index, model.getQrcode());
            if (model.getUsername() != null) statement.setString(++ index, model.getUsername());

            return statement ;
        }, holder);

        model.setId(holder.getKey().longValue());

        return model.getId();
    }

    /**
     * 查询目标链接
     * @param shortLink
     * @return
     */
    public String getTargetLink(String shortLink) {
        notNull(shortLink, "短链接参数不能为空");
        try {
            return jdbcTemplate.queryForObject("SELECT TARGET_LINK FROM TBL_SHORT_LINK WHERE SHORT_LINK = ? LIMIT 1",
                    String.class, shortLink);
        } catch (EmptyResultDataAccessException e) {

        } catch (DataAccessException e) {
            log.error("根据短链接查询目标链接出错!", e);
        }
        return null;
    }

    /**
     * 主键查询短链接信息
     * @param id
     * @return
     */
    public ShortLink get(long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM TBL_SHORT_LINK WHERE ID = ?",
                    new BeanPropertyRowMapper<ShortLink>(ShortLink.class), id);
        } catch (EmptyResultDataAccessException e) {
          return null;
        } catch (DataAccessException e) {
            log.error("主键查询短链接信息出错!", e);
            return null;
        }
    }
}
