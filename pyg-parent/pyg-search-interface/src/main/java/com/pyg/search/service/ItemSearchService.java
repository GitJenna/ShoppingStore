package com.pyg.search.service;

import java.util.List;
import java.util.Map;

/**
 * SKU搜索服务接口
 * @author Administrator
 *
 */
public interface ItemSearchService {
    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     *  删除数据
     *  @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);

    /**
     *  导入数据
     *  @param list
     */
    public void importList(List list);

}
