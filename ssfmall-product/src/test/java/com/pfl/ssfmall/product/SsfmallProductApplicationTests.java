package com.pfl.ssfmall.product;

import com.pfl.ssfmall.product.dao.AttrDao;
import com.pfl.ssfmall.product.dao.SkuInfoDao;
import com.pfl.ssfmall.product.service.CategoryService;
import com.pfl.ssfmall.product.vo.SkuItemSaleAttrVo;
import com.pfl.ssfmall.product.vo.SpuItemGroupAttrVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SsfmallProductApplicationTests {

    @Resource
    private CategoryService categoryService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private AttrDao attrDao;
    @Resource
    private SkuInfoDao skuInfoDao;


    @Test
    public void testGetCategoryFullPath() {
        Long[] path = categoryService.getFullPath(225L);
        log.info("完整路径：{}", Arrays.asList(path));
    }



    @Test
    public void testRedissonClient() {
        System.out.println(redissonClient);
    }
    @Test
    public void contextLoads() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world" + UUID.randomUUID());
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void testAttrGroupWithAttr() {
        List<SpuItemGroupAttrVo> list = attrDao.getAttrGroupWithAttr(225L, 6L);
        System.out.println(list);
    }

    @Test
    public void testSaleAttrWithAttr() {
        List<SkuItemSaleAttrVo> list = skuInfoDao.getSkuItemSaleAttr(6L);
        System.out.println(list);
    }

}
