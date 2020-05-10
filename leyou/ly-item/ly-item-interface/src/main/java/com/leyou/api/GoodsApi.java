package com.leyou.api;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuBo;
import com.leyou.pojo.SpuDetail;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {
    /**
     * 分页查询Spu
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<SpuBo> queryGoodsByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", defaultValue = "true") Boolean saleable
    );

    /**
     * 根据spu id 查询sku
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    List<Sku> querySkusBySid(@RequestParam("id") Long id);

    /**
     * 根据spu id 查询spu detail
     * @param sid
     * @return
     */
    @GetMapping("/spu/detail/{sid}")
    SpuDetail querySpuDetailBySid(@PathVariable("sid") Long sid);

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);
}
