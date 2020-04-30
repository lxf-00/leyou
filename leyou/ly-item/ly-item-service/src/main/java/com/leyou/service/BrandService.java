package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.mapper.BrandMapper;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;


    /**
     * 分页查询品牌
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        // 分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example e = new Example(Brand.class);
        Example.Criteria criteria = e.createCriteria();
        // 过滤搜索字段
        if(StringUtils.isNotBlank(key)){
            criteria.orLike("name", "%"+key+"%").orEqualTo("letter", key);
        }
        // 排序
        String orderByClause = sortBy + (desc ? " DESC": " ASC");
        e.setOrderByClause(orderByClause);
        // 查询
        List<Brand> brands = brandMapper.selectByExample(e);
        if(CollectionUtils.isEmpty(brands)){
            // 未查到相关数据
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 解析
        PageInfo info = new PageInfo<>(brands);
        // 返回
        return new PageResult<>(info.getTotal(), brands);
    }

    /**
     * 新增品牌
     * @param brand
     */
    @Transactional
    public void addBrand(Brand brand, List<Long> cids) {
        // 1， 新增品牌： tb_brand
        int count = brandMapper.insertSelective(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.BRAND_ADD_ERROR);
        }
        // 2， 新增品牌和分类中间表
        for (Long cid : cids) {
            brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    /**
     *根据品牌id,查询分类信息
     * @param bid
     * @return
     */
    public List<Category> queryCategoryByBid(Long bid) {
        List<Category> list = brandMapper.queryCategoryByBid(bid);
        return list;
    }

    /**
     * 更新品牌
     * @param brand
     * @param cids
     */
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids){
        // 1， 更新品牌
        int count = brandMapper.updateByPrimaryKey(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        // 2，删除tb_category_brand有关记录
        brandMapper.DeleteById(brand.getId());
        // 3, 重新插入（更新）
        for (Long cid : cids) {
            brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    /**
     * 删除品牌
     * @param id
     */
    @Transactional
    public void deleteBrandById(Long id) {
        // 1， 删除品牌(未设置外键，如果有设置外键，先删除中间表）
        int count = brandMapper.deleteByPrimaryKey(id);
        if(count != 1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        // 2， 删除中间表相关数据
        brandMapper.DeleteById(id);
    }

    /**
     * 根据分类id，查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryBrandByCid(cid);
        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
