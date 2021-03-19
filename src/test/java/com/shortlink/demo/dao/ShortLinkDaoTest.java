package com.shortlink.demo.dao;

import com.shortlink.demo.model.ShortLink;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShortLinkDaoTest {

    @Autowired
    private ShortLinkDao shortLinkDao;

    @Test @Transactional @Rollback
    public void save() {
        ShortLink model = new ShortLink();
        model.setShortLink(URI.create("http://t.xcjd.com/0001"));
        model.setTargetLink("http://www.xiaocaojidi.com");

        shortLinkDao.save(model);

        assertNotNull(model.getId());
    }

    @Test
    public void getTargetLink() {
        String targetLink = shortLinkDao.getTargetLink("http://t.xcjd.com/0001");
        System.out.println(targetLink);
    }
}