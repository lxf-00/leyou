package com.leyou.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "tb_specification")
public class Specification implements Serializable {
    @Id
    private Long categoryId;
    private String specifications;
}
