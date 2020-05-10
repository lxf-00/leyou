package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;


@Controller
@RequestMapping("/item")
public class PageController {

    @Autowired
    private PageService pageService;
    /**
     * 跳转到商品详情页
     * @param model
     * @return
     */
    @GetMapping("{id}.html")
    public String testThymeleaf(Model model, @PathVariable("id") Long id){
        Map<String, Object> map = pageService.loadModel(id);
        model.addAllAttributes(map);
        return "item";
    }
}
