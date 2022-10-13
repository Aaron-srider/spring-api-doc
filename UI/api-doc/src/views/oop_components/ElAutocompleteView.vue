<template>
    <div>
        <el-autocomplete
            style="width: 100%"
            class="inline-input"
            v-model="model.value"
            :fetch-suggestions="fetch_suggestions"
            :placeholder="model.placeholder"
            :trigger-on-focus="triggerOnFocus"
            @select="handle_select"
        >
            <template slot-scope="{ item }">
                <slot :row="item">{{ item[select_prop] }}</slot>
            </template>
        </el-autocomplete>
    </div>
</template>

<script>
import { OopElAutocompletionModel } from '@/lib/index';
export default {
    props: {
        model: {
            required: true,
            type: OopElAutocompletionModel,
        },
        triggerOnFocus: {
            default: true,
            type: Boolean,
        },
        select_prop: {
            required: true,
            type: String,
        },
    },
    methods: {
        fetch_suggestions(query_string, cb) {
            // 输入改变时删除已选值
            this.model.selected = {};
            this.model.fetch_suggestions(query_string, cb);
        },
        handle_select(row) {
            // 点选之后设置已选值
            this.model.value = row[this.select_prop];
            this.model.selected = row;
            this.model.do_select(row);
        },
    },
};
</script>

<style></style>
