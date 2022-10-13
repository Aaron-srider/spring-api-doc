<template>
    <el-pagination
        layout="prev, pager, next"
        :total="model.total"
        :page-size="model.pagesize"
        :current-page="model.pageno"
        @current-change="handle_pageno_change"
    ></el-pagination>
</template>

<script>
import { OopElPaginationModel } from '@/lib/index';
export default {
    props: {
        model: OopElPaginationModel,
    },
    methods: {
        // 给el-pagination绑定的翻页事件
        handle_pageno_change(pageno) {
            this.model.pageno = pageno;
            this.model.do_page();
        },
    },
};
</script>

<style></style>
