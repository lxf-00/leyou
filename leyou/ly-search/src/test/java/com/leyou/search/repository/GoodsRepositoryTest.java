package com.leyou.search.repository;


import com.leyou.common.vo.PageResult;
import com.leyou.pojo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private GoodsRepository repository;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;

    @Test
    public void testCreateIndex(){
        // 创建索引
        template.createIndex(Goods.class);
        // 配置映射
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData() throws Exception {
        int page = 1;
        int rows = 50;
        int size = 0;
        do {
            // 查询spu信息
            PageResult<SpuBo> result = goodsClient.queryGoodsByPage(page, rows, null, true);
            List<SpuBo> items = result.getItems();
            if(CollectionUtils.isEmpty(items)){
                break;
            }
            // 构建goods
            List<Goods> goodsList = items.stream().map(searchService::buildGoods).collect(Collectors.toList());
            // 存入索引库
            repository.saveAll(goodsList);
            // 翻页
            page++;
            size = items.size();
        }while(size == 50 );
    }
}
