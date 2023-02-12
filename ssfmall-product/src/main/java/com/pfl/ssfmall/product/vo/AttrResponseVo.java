package com.pfl.ssfmall.product.vo;

import lombok.Data;

@Data
public class AttrResponseVo extends AttrVo{
    /**
     * 所属分类名字
     */
    private String catelogName;
    /**
     * 所属分组名字
     */
    private String groupName;

    /**
     * 所属分类的完整路径
     */
    private Long[] catelogPath;
}
