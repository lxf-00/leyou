package com.leyou.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Table(name = "tb_brand")
public class Brand implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;// 品牌名称
    private String image;// 品牌图片
    private Character letter;

}
