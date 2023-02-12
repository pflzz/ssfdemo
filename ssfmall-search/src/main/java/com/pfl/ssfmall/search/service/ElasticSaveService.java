package com.pfl.ssfmall.search.service;

import com.pfl.common.to.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


public interface ElasticSaveService {

    /**
     * 商品上架
     * @param skuEsModels
     * @return
     */
    boolean productUp(List<SkuEsModel> skuEsModels) throws IOException;

}
