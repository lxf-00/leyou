package com.leyou.controller;

import com.leyou.pojo.Specification;
import com.leyou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specService;

    /**
     * 查询商品规格参数详情
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<String> querySpecsByCid(@PathVariable("id") Long id){
        Specification spec = specService.querySpecsByCid(id);
        return ResponseEntity.ok(spec.getSpecifications());
    }

    /**
     * 新增商品规格参数
     * @param spec
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addSpecs(Specification spec){
        specService.addSpecs(spec);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 更新商品规格参数
     * @param spec
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateSpecs(Specification spec){
        specService.updateSpecs(spec);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
