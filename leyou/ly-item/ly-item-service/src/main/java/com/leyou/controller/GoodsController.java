package com.leyou.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuBo;
import com.leyou.pojo.SpuDetail;
import com.leyou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询商品详情
     * @param page
     * @param rows
     * @param key
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBo>> queryGoodsByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", defaultValue = "true") Boolean saleable
    ){
        PageResult<SpuBo> result = goodsService.queryGoodsByPage(page, rows, key, saleable);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     * @param spuBo
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> addGoods(@RequestBody SpuBo spuBo){
        goodsService.addGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spu id 查询商品spu detail
     * @param sid
     * @return
     */
    @GetMapping("/spu/detail/{sid}")
    public ResponseEntity<SpuDetail> querySpuDetailBySid(@PathVariable("sid") Long sid){
        SpuDetail spuDetail = goodsService.querySpuDetailBySid(sid);
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据spu id 查询商品 skus
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkusBySid(@RequestParam("id") Long id){
        List<Sku> skus = goodsService.querySkusBySid(id);
        return ResponseEntity.ok(skus);
    }

    /**
     * 更新商品
     * @param spuBo
     * @return
     */
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spu id 查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }
}
