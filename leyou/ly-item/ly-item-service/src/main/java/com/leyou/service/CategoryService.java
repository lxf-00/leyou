package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.mapper.CategoryMapper;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父id，查询产品分类
     * @param pid
     * @return
     */
    public List<Category> queryByPid(Long pid) {
        Category cate = new Category();
        cate.setParentId(pid);
        List<Category> list = categoryMapper.select(cate);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据ids查询分类
     * @param ids
     * @return
     */
    public List<Category> queryCategoryByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public List<Category> queryAllByCid3(Long cid3) {
        try {
            Category c3 = categoryMapper.selectByPrimaryKey(cid3);
            Category c2 = categoryMapper.selectByPrimaryKey(c3.getParentId());
            Category c1 = categoryMapper.selectByPrimaryKey(c2.getParentId());

            return Arrays.asList(c1,c2,c3);
        }catch (Exception e){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

    }
}
