package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.mapper.SpecificationMapper;
import com.leyou.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SpecificationService {

    @Autowired
    private SpecificationMapper specMapper;

    /**
     * 查询商品规格参数
     * @param id
     * @return
     */
    public Specification querySpecsByCid(Long id) {
        Specification spec = specMapper.selectByPrimaryKey(id);
        if(spec == null){
            throw new LyException(ExceptionEnum.SPECS_NOT_FOUND);
        }
        return spec;
    }

    /**
     * 新增商品规格参数
     * @param spec
     */
    @Transactional
    public void addSpecs(Specification spec) {
        int count = specMapper.insert(spec);
        if(count != 1){
            throw new LyException(ExceptionEnum.SPECS_ADD_ERROR);
        }
    }

    /**
     * 更新商品规格参数
     * @param spec
     */
    @Transactional
    public void updateSpecs(Specification spec) {
        int count = specMapper.updateByPrimaryKey(spec);
        if(count != 1){
            throw new LyException(ExceptionEnum.SPECS_UPDATE_ERROR);
        }
    }
}
