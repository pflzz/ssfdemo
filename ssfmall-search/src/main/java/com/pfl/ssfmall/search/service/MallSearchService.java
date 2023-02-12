package com.pfl.ssfmall.search.service;

import com.pfl.ssfmall.search.vo.SearchParam;
import com.pfl.ssfmall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param param 所有可能的检索条件
     * @return 检索结果
     */
    SearchResult search(SearchParam param);
}
