package com.shortlink.demo.helper;

import org.hashids.Hashids;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.net.URI;




public class HashidsTest {

    @Test
    public void test() {
        Hashids hashids = new Hashids("BASE#2017");
        assertEquals("32ZL", hashids.encodeHex("999"));
        assertEquals("pBEOa", hashids.encodeHex("9999"));
        assertEquals("E0b9p", hashids.encodeHex("99999"));
        assertEquals("2EE79v", hashids.encodeHex("999999"));
        assertEquals("YkExxMzvXws6", hashids.encodeHex("9999999999999"));
        System.out.println(hashids.decodeHex("v10"));
        URI uri = URI.create("http://" + "t.xg2.cn" + "/" + "v10");
        System.out.println(uri.getPath());
    }

}
