package com.leyou.page.service;

import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询要返回的相关信息
 * spu
 * spudetail
 * sku集合
 * 商品分类
 * 品牌
 * sku 特有规格参数
 */
@Slf4j
@Service
public class PageService {
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 查询数据，进行封装
     * @param spuId
     * @return
     */
    public Map<String, Object> loadModel(Long spuId){
        try{
            // 查询spu
            Spu spu = goodsClient.querySpuById(spuId);
            // 查询spuDetail
            SpuDetail spuDetail = goodsClient.querySpuDetailBySid(spuId);
            // 查询sku集合
            List<Sku> skus = goodsClient.querySkusBySid(spuId);
            // 查询categories
            List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            // 获取品牌信息
            Brand brand = brandClient.queryBrandById(spu.getBrandId());

            // 组织返回结果
            Map<String,Object> map = new HashMap<>();
            map.put("spu", spu);   //  spu信息
            map.put("spuDetail", spuDetail); //  spuDetail信息
            map.put("skus", skus); //  sku集合
            map.put("categories", categories); // categories信息
            map.put("brand", brand); // 品牌信息

            // 返回结果
            return map;

        }catch (Exception e) {
            log.error("加载商品数据出错,spuId:{}", spuId, e);
        }
        return null;
    }

    /**
     * 页面静态化到指定位置
     * @param spuId
     */
    public void createHtml(Long spuId){
        // 上下文
        Context context = new Context();
        Map<String, Object> map = loadModel(spuId);
        context.setVariables(map);
        // 输出流
        File dest = new File("/Users/lxf/Desktop/", spuId + ".html");
        // 判断是否存在
        if(dest.exists()){
            // 存在删除
            dest.delete();
        }
        try{
            PrintWriter printWriter = new PrintWriter(dest, "UTF-8");
            // 生成html
            templateEngine.process("item", context, printWriter);
        } catch (Exception e){
            log.error("[静态页服务】生成静态页异常", e );
        }

    }

    /**
     * 删除静态页
     * @param spuId
     */
    public void deleteHtml(Long spuId) {
        File dest = new File("/Users/lxf/Desktop/", spuId + ".html");
        // 判断是否存在
        if(dest.exists()){
            // 存在删除
            dest.delete();
        }
    }
}
