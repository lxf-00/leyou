<template>
  <div>
    <!--卡片-->
    <v-card>
      <!--卡片头部-->
      <v-card-title>
        <!--新增按钮-->
        <v-btn color="primary" @click="addBrand">新增</v-btn>
        <!--空间隔离组件-->
        <v-spacer />
        <!--搜索框，与search属性关联-->
        <v-text-field label="输入关键字搜索" v-model="search" append-icon="search" hide-details/>
      </v-card-title>
      <!-- 分割线 -->
      <v-divider/>
      <!--表格-->
      <v-data-table
        :headers="headers"
        :items="brands"
        :pagination.sync="pagination"
        :total-items="totalBrands"
        :loading="loading"
        class="elevation-1"
      >
        <template slot="items" slot-scope="props">
          <td class="text-xs-center">{{ props.item.id }}</td>
          <td class="text-xs-center">{{ props.item.name }}</td>
          <td class="text-xs-center"><img v-if="!!props.item.image" width="102" height="36" :src="props.item.image"/></td>
          <td class="text-xs-center">{{ props.item.letter }}</td>
          <td class="justify-center layout px-0">
            <v-btn icon @click="editBrand(props.item)">
              <i class="el-icon-edit"/>
            </v-btn>
            <v-btn icon @click="deleteBrand(props.item)">
              <i class="el-icon-delete"/>
            </v-btn>
          </td>
        </template>
        <template slot="expand" slot-scope="props">
          <v-card flat>
            <v-card-text>Peek-a-boo!</v-card-text>
          </v-card>
        </template>
        <template slot="no-data">
          <v-alert :value="true" color="error" icon="warning">
            对不起，没有查询到任何数据 :(
          </v-alert>
        </template>
        <template slot="pageText" slot-scope="props">
          共{{props.itemsLength}}条,当前:{{ props.pageStart }} - {{ props.pageStop }}
        </template>
      </v-data-table>
      <!--弹出的对话框-->
      <v-dialog max-width="500" v-model="show" v-if="show">
        <v-card>
          <!--对话框的标题-->
          <v-toolbar dense dark color="primary">
            <v-toolbar-title>{{isEdit ? "更新": "新增"}}品牌</v-toolbar-title>
            <v-spacer/>
            <!--关闭窗口的按钮-->
            <v-btn icon @click="show = false"><v-icon>close</v-icon></v-btn>
          </v-toolbar>
          <!--对话框的内容，表单-->
          <v-card-text class="px-5">
            <my-brand-form @close="show = false" :oldBrand="oldBrand" :isEdit="isEdit"/>
          </v-card-text>
        </v-card>
      </v-dialog>
    </v-card>
  </div>
</template>

<script>
    import MyBrandForm from "./MyBrandForm";
    export default {
        name: "MyBrand",
        components: {
          MyBrandForm
          },
        data(){
          return{
            search: '', // 搜索过滤字段
            totalBrands: 0, // 总条数
            brands: [], // 当前页品牌数据
            loading: true, // 是否在加载中
            pagination: {}, // 分页信息
            headers: [ // 头信息
              {text: 'id', align: 'center', value: 'id'},
              {text: '名称', align: 'center', sortable: false, value: 'name'},
              {text: 'LOGO', align: 'center', sortable: false, value: 'image'},
              {text: '首字母', align: 'center', value: 'letter', sortable: true,},
              {text: '操作', align: 'center', value: 'id', sortable: false}
            ],
            show: false,  // 弹窗控制
            oldBrand: {},
            isEdit: false,
          }
        },
      // 渲染后加载
      mounted(){ // 渲染后执行
        // 查询数据
        this.getDataFromServer();
      },
      // 监视
      watch:{
          // 分页信息
          pagination:{
            deep: true,
            handler(){
              this.getDataFromServer();
            }
          },
        // 搜索信息
        search:{
            deep: true,
          handler(){
              this.pagination.page = 1;
            this.getDataFromServer();
          }
        },
        show(val, oldVal) {
          // 表单关闭后重新加载数据
          if (oldVal) {
            this.getDataFromServer();
          }
        }
      },
      methods:{
        // 服务器端加载数据
        getDataFromServer(){
          this.$http.get("/item/brand/page",{
            params:{
              key: this.search, // 搜索条件
              page: this.pagination.page,// 当前页
              rows: this.pagination.rowsPerPage,// 每页大小
              sortBy: this.pagination.sortBy,// 排序字段
              desc: this.pagination.descending// 是否降序
            }
          }).then(resp => { // 这里使用箭头函数
            // 将得到的数据赋值给本地属性
            this.brands = resp.data.items;
            this.totalBrands = resp.data.total;
            // 完成赋值后，把加载状态赋值为false
            this.loading = false;
          })
        },
        // 新增品牌
        addBrand(){
          this.show = true;
          this.isEdit = false;
        },
        // 更新品牌
        editBrand(oldBrand){
          // 根据品牌信息查询商品分类
          this.$http.get("/item/brand/bid/" + oldBrand.id)
            .then(resp => {
              this.isEdit = true;
              // 控制弹窗可见：
              this.show = true;
              // 获取要编辑的brand
              this.oldBrand = oldBrand
              // 回显商品分类
              this.oldBrand.categories = resp.data;
            })
        },

        // 删除品牌
        deleteBrand(item){
          this.$message.confirm('此操作将永久删除该品牌, 是否继续?').then(() => {
            this.$http.delete('/item/brand?id=' + item.id)
              .then(() => {
                // 删除成功，重新加载数据
                this.$message.success("删除成功！");
                this.getDataFromServer();
              })
              .catch(() => {
                this.$message.info("删除已取消！");
              });
          })
        },
      },
    }
</script>

<style scoped>

</style>
