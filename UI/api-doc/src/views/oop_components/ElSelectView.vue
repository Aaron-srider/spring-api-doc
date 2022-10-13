<template>
    <el-select
        :id="model.id"
        v-loading="model.loading"
        v-model="model.value"
        style="width: 100%"
        :placeholder="placeholder"
        @visible-change="handle_select"
    >
        <slot></slot>
    </el-select>
</template>

<script>
import { OopElSelectModel } from '@/lib';
export default {
    props: {
        loading: {
            type: Boolean,
            default: false,
        },
        placeholder: {
            type: String,
            default: '',
        },
        model: {
            required: true,
            type: OopElSelectModel,
        },
    },
    methods: {
        handle_select(show) {
            if (!show) {
                let select = $(`#${this.model.id}`);
                select.blur();
            }
        },
    },
};
</script>

<style></style>
