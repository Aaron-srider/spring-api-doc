<template>
    <el-table v-loading="model.loading" :data="model.data" style="width: 100%">
        <el-table-column
            v-for="col in model.cols"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :width="col.width == undefined ? 0 : col.width"
        >
            <template slot-scope="scope">
                <slot :row="scope.row" :col="col">
                    {{ col.prop == undefined ? '' : scope.row[col.prop] }}
                </slot>
            </template>
        </el-table-column>
    </el-table>
</template>

<script>
import { OopElTableModel } from '@/lib/index';
export default {
    props: {
        model: {
            type: OopElTableModel,
        },
    },
};
</script>

<style></style>
