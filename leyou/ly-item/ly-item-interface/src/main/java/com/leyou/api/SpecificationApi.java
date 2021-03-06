package com.leyou.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/spec")
public interface SpecificationApi {
    @GetMapping("{id}")
    String querySpecsByCid(@PathVariable("id") Long id);
}
