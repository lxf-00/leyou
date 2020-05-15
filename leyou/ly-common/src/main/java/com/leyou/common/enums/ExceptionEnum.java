package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum  ExceptionEnum {

    CATEGORY_NOT_FOUND(404, "商品分类信息不存在"),

    BRAND_NOT_FOUND(404, "品牌信息不存在"),
    BRAND_ADD_ERROR(500, "新增品牌失败"),
    BRAND_UPDATE_ERROR(500, "更新品牌失败"),
    BRAND_DELETE_ERROR(500, "删除品牌失败"),

    FILE_UPLOAD_ERROR(500, "图片上传失败"),
    INVALID_FILE_TYPE(400, "无效文件格式"),

    SPECS_NOT_FOUND(404, "商品规格详情不存在"),
    SPECS_ADD_ERROR(500, "商品规格参数新增失败"),
    SPECS_UPDATE_ERROR(500, "商品规格参数更新失败"),

    GOODS_NOT_FOUND(404, "商品不存在"),

    SPU_UPDATE_ERROR(500, "商品SPU新增失败"),
    SPUDETAIL_UPDATE_ERROR(500, "商品SPU新增失败"),
    SPUDETAIL_NOT_FOUND(404, "商品SPU详情不存在"),
    SKU_UPDATE_ERROR(500, "商品新增失败"),
    SKU_NOT_FOUND(404, "商品SKU不存在"),
    STOCK_UPDATE_ERROR(500, "库存新增失败"),
    INVALID_SPU_ERROR(500, "无效商品SPU"),
    INVALID_USER_DATA_TYPE(400, "无效的用户相关信息"),
    INVALID_VERIFY_CODE(400, "无效的验证码"),
    INVALID_USERNAME_PASSWORD(400, "用户名或密码错误!"),
    CREATE_TOKEN_ERROT(500,"用户凭证生成失败"),
    UNAUTHORIZED(403, "未授权"),
    CART_NOT_FOUND(400, "购物车相关信息不存在"),
    ;
    private int code;
    private String msg;
}
