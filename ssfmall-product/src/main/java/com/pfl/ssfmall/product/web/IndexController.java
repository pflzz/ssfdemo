package com.pfl.ssfmall.product.web;

import com.pfl.ssfmall.product.entity.CategoryEntity;
import com.pfl.ssfmall.product.service.CategoryService;
import com.pfl.ssfmall.product.vo.Catelog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 1. 查询所有以及分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1List();
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    /**
     * 获取 2，3 子分类数据
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public  Map<Long, List<Catelog2Vo>> catalogPage() {
        Map<Long, List<Catelog2Vo>> catelog2VoMap = categoryService.getCatalogJson();
        return catelog2VoMap;
    }
}
