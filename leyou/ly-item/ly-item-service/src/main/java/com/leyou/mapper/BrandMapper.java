package com.leyou.mapper;


import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>, IdListMapper<Brand, Long> {
    @Insert("insert into tb_category_brand(category_id, brand_id)values(#{cid}, #{bid}) ")
    void insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    @Select("select * from tb_category tc inner join tb_category_brand cb on tc.id = cb.category_id where cb.brand_id = #{bid}")
    List<Category> queryCategoryByBid(@Param("bid") Long bid);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    void DeleteById(@Param("bid") Long bid);

    @Select("select b.* from tb_brand b left join tb_category_brand cb on b.id = cb.brand_id where cb.category_id = #{cid}")
    List<Brand> queryBrandByCid(@Param("cid")Long cid);
}
