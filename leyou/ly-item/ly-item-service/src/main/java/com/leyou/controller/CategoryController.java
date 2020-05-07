package com.leyou.controller;

import com.leyou.pojo.Category;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id,查询产品分类
     * @param pid
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Category>> queryByPid(@RequestParam(name = "pid", required = true) Long pid){
        return ResponseEntity.ok(categoryService.queryByPid(pid));
    }

    /**
     * 根据ids查询分类
     * @param ids
     * @return
     */
    @GetMapping("/list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryCategoryByIds(ids));
    }

    /**
     * 根据cid3，查询所有上级分类信息
     * @param cid3
     * @return
     */
    @GetMapping("/all/level")
    public ResponseEntity<List<Category>> queryAllByCid3(@RequestParam("cid3") Long cid3){
        return  ResponseEntity.ok(categoryService.queryAllByCid3(cid3));
    }
}
