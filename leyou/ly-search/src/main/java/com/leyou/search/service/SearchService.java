package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.stream.Collectors;

/**
 * 导入数据： spu 转为goods
 */
@Slf4j
@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;
    
    @Autowired
    private GoodsClient goodsClient;
    
    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 导入数据到elasticsearch
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu){
        Goods goods = new Goods();
        Long spuId = spu.getId();

        // 查询商品分类名称
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if(CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> cnames = categories.stream().map(Category::getName).collect(Collectors.toList());
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 搜索字段
        String all = spu.getTitle() + StringUtils.join(cnames, " ") + brand.getName();

        // 查询sku
        List<Sku> skuList = goodsClient.querySkusBySid(spuId);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        // 处理skuList
        Set<Long> priceSet = new HashSet<>();
        List<Map<String, Object>> skus = new ArrayList<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("images", StringUtils.substringBefore(sku.getImages(), ","));
            skus.add(map);

            // 处理价格
            priceSet.add(sku.getPrice());
        }

        // 查询规格参数
        SpuDetail spuDetail = goodsClient.querySpuDetailBySid(spuId);
        if(spuDetail == null){
            throw  new LyException(ExceptionEnum.SPUDETAIL_NOT_FOUND);
        }
        String specifications = spuDetail.getSpecifications();
        // 处理规格参数
        Map<String, Object> specs = new HashMap<>();
        List<Map<String,Object>> maps = JsonUtils.nativeRead(specifications, new TypeReference<List<Map<String, Object>>>() {});
        maps.forEach(map ->{
            List<Map<String,Object>> params = (List<Map<String,Object>>)map.values().toArray()[1];
            params.forEach(param ->{
                if((Boolean)param.get("searchable")){
                    String k = (String)param.get("k");
                    Object v = null;
                    Object unit = null;
                    Object options = null;
                    try {
                        v = param.get("v");
                        unit = param.get("unit");
                        options = param.get("options");
                    } catch (Exception e){

                    }finally {
                       if(unit != null){
                           v = v + (String)unit;
                       }
                       if(options != null){
                           v = options;
                       }
                        specs.put(k, v);
                    }
                }
            });
        });



        // 处理goods
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setAll(all); // 搜索字段 包括标题，品牌，分类，规格等
        goods.setPrice(priceSet); // 所有sku 价格的集合
        goods.setSkus(JsonUtils.serialize(skus));  //  所有sku的集合的json格式
        goods.setSpecs(specs);  // 所有可搜索规格参数
        goods.setSubTitle(spu.getSubTitle());
        return  goods;
    }

    /**
     * 查询
     * @param searchRequest
     * @return
     */
    public PageResult<Goods> search(SearchRequest searchRequest) {
        Integer page = searchRequest.getPage() - 1;
        Integer size = searchRequest.getSize();
        String sortBy = searchRequest.getSortBy();
        String key = searchRequest.getKey();
        if(StringUtils.isBlank(key)){
            return null;
        }
        // 1,创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2,结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 3,分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        // 4,过滤

        BoolQueryBuilder basicQuery = buildBasicQuery(searchRequest);;
        queryBuilder.withQuery(basicQuery);
        // 5,聚合分类和品牌信息
        // 5.1 聚合分类
        String categoryAgg = "categoryAgg";
        String brandAgg = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms( categoryAgg).field("cid3"));
        // 5.2 聚合品牌
        queryBuilder.addAggregation(AggregationBuilders.terms( brandAgg).field("brandId"));
        // 6. 排序
        if(StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy)
                    .order(searchRequest.getDescending() ? SortOrder.DESC : SortOrder.ASC));
        }
        // 7. 查询
        // Page<Goods> results = repository.search(queryBuilder.build());
        AggregatedPage<Goods> results = template.queryForPage(queryBuilder.build(), Goods.class);
        // 8. 解析结果
        // 8.1 分页结果
        List<Goods> goodsList = results.getContent();
        Long totalPages = (long) results.getTotalPages();
        long total = results.getTotalElements();
        // 8.2 聚合结果
        Aggregations aggs = results.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAgg));
        List<Brand> brands = parseBrandAgg(aggs.get(brandAgg));

        // 9， 完成规格参数聚合
        List<Map<String, Object>> specs = null;
        if(categories != null && categories.size() == 1){
            // 商品分类存在，且数量为1，可以聚合
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(total, totalPages, goodsList, categories, brands, specs);
    }
    // 过滤处理
    private BoolQueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌要特殊处理
            if (!"cid3".equals(key) &&  !"brandId".equals(key)) {
                key = "specs." + key + ".keyword";
            }
            // 字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
        }
        // 添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }

    // 规格参数聚合
    private List<Map<String,Object>> buildSpecificationAgg(Long id, BoolQueryBuilder basicQuery) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String,Object>> tempList = new ArrayList<>();
        // 1, 查询需要聚合的规格参数
        String strSpec = specificationClient.querySpecsByCid(id);
        List<Map<String,Object>> maps = JsonUtils.nativeRead(strSpec, new TypeReference<List<Map<String, Object>>>() {
        });
        for (Map<String, Object> map : maps) {
            List<Map<String,Object>> params = (List<Map<String,Object>>)map.get("params");
            params.forEach(e -> {
                Boolean flag = (Boolean)e.get("searchable");
                if(flag){
                    tempList.add(e);
                }
            });
        }
        // 2，聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        for (Object param : tempList) {
            Map<String,Object> p =  (Map<String,Object>)param;
            String k = String.valueOf( p.get("k"));
            queryBuilder.addAggregation(AggregationBuilders.terms(k).field("specs."+k+".keyword"));
        }
        // 3， 解析结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggs = result.getAggregations();
        for (Object param :tempList) {
            Map<String,Object> p = (Map<String,Object>) param;
            String k = (String) p.get("k");
            StringTerms terms = aggs.get(k);
            List<String> options = terms.getBuckets().stream().map(b -> b.getKeyAsString())
                    .collect(Collectors.toList());
            // 组织map
            Map<String,Object> map = new HashMap<>();
            map.put("k", k);
            map.put("options", options);
            list.add(map);
        }
        return list;
    }

    // 处理分类聚合
    private List<Category> parseCategoryAgg(LongTerms aggregation) {
        try {
            List<Long> ids = aggregation.getBuckets()
                    .stream().map(c -> c.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        }catch (Exception e) {
            log.error("[搜索服务]分类查询出错", e );
            return null;
        }
    }

    // 处理品牌聚合
    private List<Brand> parseBrandAgg(LongTerms aggregation) {
        try {
            List<Long> ids = aggregation.getBuckets()
                    .stream().map(b -> b.getKeyAsNumber()
                            .longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        }catch (Exception e){
            log.error("[搜索服务]品牌查询出错", e );
            return  null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 构建goods
        Goods goods = buildGoods(spu);
        // 存入索引库
        repository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        repository.deleteById(spuId);
    }
}
