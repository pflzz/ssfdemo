package com.pfl.ssfmall.search;

import com.alibaba.fastjson.JSON;
import com.pfl.ssfmall.search.config.SsfmallElasticSearchConfig;
import lombok.Data;
import net.minidev.json.JSONArray;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class testElasticSearchConfig {
    @Resource
    private RestHighLevelClient client;

    @Test
    public void indexData() throws IOException {
        IndexRequest index = new IndexRequest("users");
        index.id("1");
        User user = new User();
        user.setUsername("uzi");
        user.setAge(18);
        user.setGender("男");


        String jsonString = JSON.toJSONString(user);
        System.out.println(jsonString);
        index.source(jsonString, XContentType.JSON);

        IndexResponse indexResponse = client.index(index, SsfmallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(indexResponse);

    }
    @Data
    class User {
        private String username;
        private Integer age;
        private String gender;
    }

    @Test
    public void testClient() {
        System.out.println(client);
    }
}
