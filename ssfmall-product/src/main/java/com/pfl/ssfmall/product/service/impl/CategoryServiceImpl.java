package com.pfl.ssfmall.product.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;
import com.pfl.ssfmall.product.dao.CategoryDao;
import com.pfl.ssfmall.product.entity.CategoryEntity;
import com.pfl.ssfmall.product.service.CategoryBrandRelationService;
import com.pfl.ssfmall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.pfl.ssfmall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryDao categoryDao;
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> getCategoryWithTree() {
        // 1. 查询所有分类信息
        List<CategoryEntity> categoryEntities = categoryDao.selectList(null);

        // 2. 递归查询每一层级的分类信息
        List<CategoryEntity> levelMenu = categoryEntities.stream().filter(item ->
                // 找到最顶级的分类
                item.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
        ).collect(Collectors.toList());

        return levelMenu;
    }

    /**
     * 获取 catelogId 分类完整的父路径信息
     *
     * @param catelogId 目标分类
     * @return
     */
    @Override
    public Long[] getFullPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        getParentsPath(catelogId, path);
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    @CacheEvict(value = "category", allEntries = true)
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'level1List'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCatelog(category.getCatId(), category.getName());
    }

    /**
     * 获取所有一级分类
     *
     * @return
     */
    @Cacheable(value = {"category"}, key = "'level1List'")
    @Override
    public List<CategoryEntity> getLevel1List() {
        List<CategoryEntity> entityList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entityList;
    }

    /**
     * 获取 2，3 子分类数据 (SpringCache)
     */
    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<Long, List<Catelog2Vo>> getCatalogJson() {
        // 查询所有数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        /**
         * 优化：查询一次数据库
         */
        // 获取所有分类的数据 k - v (一级分类的 id - 二级 vo 的数据)
        List<CategoryEntity> level1List = getLevel1List();
        Map<Long, List<Catelog2Vo>> map = level1List.stream().collect(Collectors.toMap(k -> k.getCatId(), item -> {
            List<CategoryEntity> categoryEntities2 = gerCategoryEntityByParentCid(categoryEntities, item.getParentCid());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities2 != null) {
                // 封装二级分类数据
                catelog2Vos = categoryEntities2.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo();
                    catelog2Vo.setCatalog1Id(item.getCatId().toString());

                    List<CategoryEntity> categoryEntities3 = gerCategoryEntityByParentCid(categoryEntities, l2.getParentCid());

                    // 封装三级分类数据
                    List<Catelog2Vo.Catelog3Vo> catalog3Vos = categoryEntities3.stream().map(l3 -> {
                        Catelog2Vo.Catelog3Vo catalog3Vo = new Catelog2Vo.Catelog3Vo();
                        catalog3Vo.setCatalog2Id(l2.getCatId().toString());
                        catalog3Vo.setId(l3.getCatId().toString());
                        catalog3Vo.setName(l3.getName());
                        return catalog3Vo;
                    }).collect(Collectors.toList());
                    catelog2Vo.setCatalog3List(catalog3Vos);
                    catelog2Vo.setId(l2.getCatId().toString());
                    catelog2Vo.setName(l2.getName());
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return map;
    }

    /**
     * 获取 2，3 子分类数据 (缓存逻辑)
     */

    public Map<Long, List<Catelog2Vo>> getCatalogJson2() {

        // 查询缓存
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");

        // 如果缓存中不存在
        if (StringUtils.isEmpty(catalogJson)) {
            // 从数据库查询结果
            Map<Long, List<Catelog2Vo>> jsonFromDb = getCatalogJsonFromDbWithRedissonLock();

            return jsonFromDb;
        }
        // 如果缓存中存在则将 获取到的 json 字符串数据转化为目标对象
        Map<Long, List<Catelog2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<Long, List<Catelog2Vo>>>() {
        });
        return map;
    }

    /**
     * 从数据库获取获取 2，3 子分类数据 (redisson 分布式锁)
     */
    public Map<Long, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        // 加锁
        RLock lock = redissonClient.getLock("catalogJsonLock");
        lock.lock();
        // 查询数据库
        Map<Long, List<Catelog2Vo>> jsonFromDb;
        try {
            jsonFromDb = getCatalogJsonFromDb();
        } finally {
           lock.unlock();
        }
        return jsonFromDb;
    }

    /**
     * 从数据库获取获取 2，3 子分类数据 (分布式锁)
     */
    public Map<Long, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        // 1. 去 redis 占坑
        String lockValue = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", lockValue, 300, TimeUnit.SECONDS);
        // 如果拿到了锁
        if (lock) {
            // 查询数据库
            Map<Long, List<Catelog2Vo>> jsonFromDb;
            try {
                jsonFromDb = getCatalogJsonFromDb();
            } finally {
                // 释放锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), lockValue);
            }
            return jsonFromDb;
        } else {
            // 没拿到锁,等待 100ms 重重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithLocalLock(); // 自旋的方式
        }
    }

    private Map<Long, List<Catelog2Vo>> getCatalogJsonFromDb() {
        // 得到锁以后，我们应该在缓存中再确定一次，如果没有继续查询
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            Map<Long, List<Catelog2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<Long, List<Catelog2Vo>>>() {
            });
            return map;
        }


        // 查询所有数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        /**
         * 优化：查询一次数据库
         */
        // 获取所有分类的数据 k - v (一级分类的 id - 二级 vo 的数据)
        List<CategoryEntity> level1List = getLevel1List();
        Map<Long, List<Catelog2Vo>> map = level1List.stream().collect(Collectors.toMap(k -> k.getCatId(), item -> {
            List<CategoryEntity> categoryEntities2 = gerCategoryEntityByParentCid(categoryEntities, item.getParentCid());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities2 != null) {
                // 封装二级分类数据
                catelog2Vos = categoryEntities2.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo();
                    catelog2Vo.setCatalog1Id(item.getCatId().toString());

                    List<CategoryEntity> categoryEntities3 = gerCategoryEntityByParentCid(categoryEntities, l2.getParentCid());

                    // 封装三级分类数据
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = categoryEntities3.stream().map(l3 -> {
                        Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo();
                        catelog3Vo.setCatalog2Id(l2.getCatId().toString());
                        catelog3Vo.setId(l3.getCatId().toString());
                        catelog3Vo.setName(l3.getName());
                        return catelog3Vo;
                    }).collect(Collectors.toList());
                    catelog2Vo.setCatalog3List(catelog3Vos);
                    catelog2Vo.setId(l2.getCatId().toString());
                    catelog2Vo.setName(l2.getName());
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        // 将结果转化为 json 字符串添加到缓存
        String jsonString = JSON.toJSONString(map);
        redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
        return map;
    }


    /**
     * 从数据库获取获取 2，3 子分类数据 (本地锁)
     */
    public Map<Long, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        // 本地锁
        synchronized (this) {
            return getCatalogJsonFromDb();
        }
    }

    public List<CategoryEntity> gerCategoryEntityByParentCid(List<CategoryEntity> list, Long patentCid) {
        return list.stream().filter(item -> item.getParentCid() == patentCid).collect(Collectors.toList());
    }

    public void getParentsPath(Long categoryId, List<Long> paths) {
        paths.add(categoryId);
        CategoryEntity category = this.getById(categoryId);
        if (category.getParentCid() != 0) {
            getParentsPath(category.getParentCid(), paths);
        }

    }

    /**
     * 递归获取分类信息的子分类信息
     */
    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(menu ->
                menu.getParentCid() == root.getCatId()).map(child -> {
            child.setChildren(getChildren(child, all));
            return child;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
        ).collect(Collectors.toList());
        return children;
    }

}