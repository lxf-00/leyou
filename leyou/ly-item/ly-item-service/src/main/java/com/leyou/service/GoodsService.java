package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.mapper.*;
import com.leyou.pojo.*;
import com.leyou.pojo.Stock;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 查询 - 分页查询商品详情
     * @param page 页码
     * @param rows 每页显示条数
     * @param key  搜索字段
     * @return
     */
    public PageResult<SpuBo> queryGoodsByPage(Integer page, Integer rows, String key , Boolean saleable) {
        // 1， 分页
        PageHelper.startPage(page, rows);
        // 2， 过滤
        Example e = new Example(Spu.class);
        Example.Criteria criteria = e.createCriteria();
        // 过滤搜索字段
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title", "%"+key+"%");
        }
        // 过滤上下架
        if(saleable != null){
            criteria.andEqualTo("saleable", saleable);
        }
        // 3， 查询Spu
        List<Spu> spus = spuMapper.selectByExample(e);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 4， 先处理结果集
        PageInfo info = new PageInfo<Spu>(spus);

        // 5， 处理Spu SpuBo cname bname
        List<SpuBo> spuBos = spus.stream().map(
                spu -> {
                    // 转换为spuBo
                    SpuBo spuBo = new SpuBo();
                    BeanUtils.copyProperties(spu, spuBo);
                    // 处理cname
                    List<String> names = categoryMapper.selectByIdList(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                            .stream().map(Category::getName).collect(Collectors.toList());
                    spuBo.setCname(StringUtils.join(names, "/"));
                    // 处理bname
                    String bname = brandMapper.selectByPrimaryKey(spu.getBrandId()).getName();
                    spuBo.setBname(bname);
                    return spuBo;
                }).collect(Collectors.toList());
        // 返回结果
        return new PageResult<SpuBo>(info.getTotal(), spuBos);
    }

    /**
     * 新增 - 商品
     * @param spuBo
     */
    @Transactional
    public void addGoods(SpuBo spuBo) {
        // 新增spu
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());

        int count = spuMapper.insert(spuBo);
        if(count != 1){
            throw new LyException(ExceptionEnum.SPU_UPDATE_ERROR);
        }
        // 新增spuDetail
        spuBo.getSpuDetail().setSpuId(spuBo.getId());
        int count1 = spuDetailMapper.insert(spuBo.getSpuDetail());
        if(count1 != 1){
            throw new LyException(ExceptionEnum.SPUDETAIL_UPDATE_ERROR);
        }
        // 新增sku和stock
        saveSkuAndStock(spuBo.getSkus(), spuBo.getId());
        amqpTemplate.convertAndSend("item.insert", spuBo.getId());
    }

    // 新增sku和stock
    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(spuId);
            // 默认不参与任何促销
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            int count2 = skuMapper.insert(sku);
            if(count2 != 1){
                throw new LyException(ExceptionEnum.SKU_UPDATE_ERROR);
            }

            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            int count3 = stockMapper.insert(stock);
            if(count3 != 1){
                throw new LyException(ExceptionEnum.STOCK_UPDATE_ERROR);
            }

        }
    }

    /**
     * 查询 - spu detail
     * @param sid 商品spu id
     * @return
     */
    public SpuDetail querySpuDetailBySid(Long sid) {
        SpuDetail detail = spuDetailMapper.selectByPrimaryKey(sid);
        if(detail == null){
            throw new LyException(ExceptionEnum.SPUDETAIL_NOT_FOUND);
        }
        return  detail;
    }

    /**
     * 查询 - skus
     * @param id 商品spu id
     * @return
     */
    public List<Sku> querySkusBySid(Long id) {
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> skus = skuMapper.select(record);

        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        // 处理库存信息
        skus = skus.stream().map(sku -> {
            Integer stock = stockMapper.selectByPrimaryKey(sku.getId()).getStock();
            sku.setStock(stock);
            return sku;
        }).collect(Collectors.toList());

        return skus;
    }

    /**
     * 更新 - 商品
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        // 1， 更新Spu
        // 判断spu是否有效
        if(!spuBo.getValid()){
           throw new LyException(ExceptionEnum.INVALID_SPU_ERROR);
        }
        spuBo.setLastUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKey(spuBo);
        if(count != 1){
            throw new LyException(ExceptionEnum.SPU_UPDATE_ERROR);
        }
        // 2， 更新SpuDetail
        int count1 = spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        if(count1 != 1){
            throw new LyException(ExceptionEnum.SPUDETAIL_UPDATE_ERROR);
        }
        // 3， 更新Sku（spuId, id) stock
        if(CollectionUtils.isEmpty(spuBo.getSkus())){
            // 传递的sku为空
            // 判断原sku是否为空
            Sku sku = new Sku();
            sku.setSpuId(spuBo.getId());
            if(CollectionUtils.isEmpty(skuMapper.select(sku))){
                // 原sku 也为空：什么也不做
                return;
            }
            // 删除原有sku
            skuMapper.deleteByPrimaryKey(spuBo.getId());
        }
        updateSkuAndStock(spuBo.getSkus(), spuBo.getId());
        // 发送mq信息
        amqpTemplate.convertAndSend("item.update", spuBo.getId());
    }
    // 更新sku和stock
    private void updateSkuAndStock(List<Sku> skus, Long spuId) {
        // 1, 获取原有sku id 集合
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Long> oldIds = skuMapper.select(sku).stream().map(s ->{ return s.getId(); }).collect(Collectors.toList());
        // 2， 获取传递过来的id
        List<Long> newIds = skus.stream().map(s -> {return s.getId();}).collect(Collectors.toList());
       // 3，删除旧集合中有，新集合中没有的数据
       oldIds.stream().map(id ->{
           if(! newIds.contains(id)){
               // 删除该sku
               skuMapper.deleteByPrimaryKey(id);
               // 删除相关的stock信息
               stockMapper.deleteByPrimaryKey(id);
               }
           return null; });
       // 4，更新剩下的数据
        skus.stream().map(s ->{
            // 更新sku
            s.setSpuId(spuId);
            s.setLastUpdateTime(new Date());
            skuMapper.updateByPrimaryKeySelective(s);
            // 更新相关的库存
            Stock stock = new Stock();
            stock.setStock(s.getStock());
            stock.setSkuId(s.getId());
            stockMapper.updateByPrimaryKeySelective(stock);
            return null;
        });

    }

    /**
     * 根据spu id 查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu == null){
            throw new LyException(ExceptionEnum.INVALID_SPU_ERROR);
        }
        return spu;
    }
}
