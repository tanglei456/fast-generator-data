<template>
    <el-autocomplete
      v-model="state"
      value-key="name"
      :fetch-suggestions="querySearch"
      popper-class="my-autocomplete"
      placeholder="Please input"
      @select="handleSelect"
    >
      <template #suffix>
        <el-icon class="el-input__icon" @click="handleIconClick">
          <edit />
        </el-icon>
      </template>
      <template #default="{ item }">
        <div class="value">{{ item.name }}</div>
        <span class="link">{{ item.link }}</span>
      </template>
    </el-autocomplete>
  </template>
  
  <script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { Edit } from '@element-plus/icons-vue'
  
  interface LinkItem {
    name: string
    link: string
  }
  
  const state = ref('')
  const links = ref<LinkItem[]>([])
  
  const querySearch = (queryString: string, cb:any) => {
    const results = queryString
      ? links.value.filter(createFilter(queryString))
      : links.value
    // call callback function to return suggestion objects
    cb(results)
  }

  const createFilter = (queryString: any) => {
	return (restaurant: any) => {
		//模糊匹配
		return restaurant.name.toLowerCase().match(queryString.toLowerCase()) ? true : false
	}
}
  const loadAll = () => {
    return [
      { name: 'vue', link: 'https://github.com/vuejs/vue' },
      { name: 'element', link: 'https://github.com/ElemeFE/element' },
      { name: 'cooking', link: 'https://github.com/ElemeFE/cooking' },
      { name: 'mint-ui', link: 'https://github.com/ElemeFE/mint-ui' },
      { name: 'vuex', link: 'https://github.com/vuejs/vuex' },
      { name: 'vue-router', link: 'https://github.com/vuejs/vue-router' },
      { name: 'babel', link: 'https://github.com/babel/babel' },
    ]
  }
  const handleSelect = (item: LinkItem) => {
    console.log(item)
  }
  
  const handleIconClick = (ev: Event) => {
    console.log(ev)
  }
  
  onMounted(() => {
    links.value = loadAll()
  })
  </script>
  
  <style>
  .my-autocomplete li {
    line-height: normal;
    padding: 7px;
  }
  .my-autocomplete li .name {
    text-overflow: ellipsis;
    overflow: hidden;
  }
  .my-autocomplete li .addr {
    font-size: 12px;
    color: #b4b4b4;
  }
  .my-autocomplete li .highlighted .addr {
    color: #ddd;
  }
  </style>
  