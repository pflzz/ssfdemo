package com.pfl.ssfmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pfl.common.to.SkuEsModel;
import com.pfl.ssfmall.search.config.SsfmallElasticSearchConfig;
import com.pfl.ssfmall.search.constant.EsConstant;
import com.pfl.ssfmall.search.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ElasticSaveServiceImpl implements ElasticSaveService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到 es

        // 1. 给es 建立索引 product 建立好映射关系

        // 2. 给 es 保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            // 1. 构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, SsfmallElasticSearchConfig.COMMON_OPTIONS);

        boolean failures = bulk.hasFailures();
        String failureMessage = bulk.buildFailureMessage();
        log.error("错误原因 => {}", failureMessage);
        List<Integer> collect = Arrays.stream(bulk.getItems()).map(item -> item.getItemId()).collect(Collectors.toList());
        log.info("商品上架完成：{} 返回数据: {}", collect, bulk);

        return failures;
    }
}
