package com.pfl.ssfmall.search.controller;

import com.pfl.common.utils.R;
import com.pfl.ssfmall.search.service.MallSearchService;
import com.pfl.ssfmall.search.vo.SearchParam;
import com.pfl.ssfmall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private MallSearchService mallSearchService;


    /**
     *
     * @param param 所有可能的检索条件
     * @return 检索结果
     */
    @GetMapping()
    public R listPage(SearchParam param) {

        SearchResult result = mallSearchService.search(param);
        return R.ok().put("data", result);
    }


}
