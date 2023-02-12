

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@ContextConfiguration
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApplication {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void testRedis() {
        stringRedisTemplate.opsForValue().set("1", "2");
    }
}
