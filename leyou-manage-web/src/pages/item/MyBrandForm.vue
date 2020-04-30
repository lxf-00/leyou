<template>
  <!--表单-->
  <v-form v-model="valid" ref="MyBrandForm">
    <!--品牌-->
    <v-text-field
      v-model="brand.name"
      label="请输入品牌名称"
      required
      :rules="[v => !!v || '品牌名称不能为空']"
      :counter="10"
    />
    <!--首字母-->
    <v-text-field
      label="请输入品牌首字母"
      v-model="brand.letter"
      :rules="[v => v.length === 1 || '首字母只能是1位']"
      mask="A"
      required />
    <!--分类信息-->
    <v-cascader
      url="/item/category/list"
      multiple
      v-model="brand.categories"
      required
      label="请选择商品分类"/>
    <!--文件上传-->
    <v-layout row>
      <v-flex xs3>
        <span style="font-size: 16px">品牌LOGO：</span>
      </v-flex>
      <v-flex>
        <v-upload
          url="/upload/image"
          v-model="brand.image"
          :multiple="false"
          :pic-width="250"
          :pic-height="90"
        />
      </v-flex>
    </v-layout>
    <!--提交和重置-->
    <v-layout class="my-4" row>
      <v-spacer/>
      <v-btn @click="submit" color="primary">提交</v-btn>
      <v-btn @click="clear" >重置</v-btn>
    </v-layout>
  </v-form>


</template>

<script>
    export default {
        name: "my-brand-form",
        props:{
          oldBrand: Object,
          isEdit:{
            type: Boolean,
            default: false,
          }
        },
        data() {
          return {
            valid: false, // 表单校验结果标记
            brand: {
              name: '', // 品牌名称
              letter: '', // 品牌首字母
              image: '',// 品牌logo
              categories: [], // 品牌所属的商品分类数组
            },
          }
        },
     mounted(){
          if(this.isEdit){
            this.brand = Object.deepCopy(this.oldBrand);
          }
     },
      methods:{
          // 提交表单
          submit() {
            // 1、表单校验
            if (this.$refs.MyBrandForm.validate()) {
              // 2、定义一个请求参数对象，通过解构表达式来获取brand中的属性
              const {categories ,letter ,...params} = this.brand;
              // 3、数据库中只要保存分类的id即可，因此我们对categories的值进行处理,只保留id，并转为字符串
              params.cids = categories.map(c => c.id).join(",");
              // 4、将字母都处理为大写
              params.letter = letter.toUpperCase();
              // 5、将数据提交到后台
              this.$http({
                method: this.isEdit ? "put":"post",
                url: "/item/brand",
                data: this.$qs.stringify(params)
              })
                .then(() => {
                  // 6、弹出提示
                  this.$message.success("保存成功！");
                  this.closeWindow();
                })
                .catch(() => {
                  this.$message.error("保存失败！");
                });
            }
        },
        clear(){
          // 重置表单
          this.$refs.MyBrandForm.reset();
          // 需要手动清空商品分类
          this.categories = [];
        },
        closeWindow(){
          this.$emit("close");
        },
        handleImageSuccess(res) {
          this.brand.image = res;
        },
        removeImage(){
          this.brand.image = "";
        },
      }
    }
</script>

<style scoped>

</style>
