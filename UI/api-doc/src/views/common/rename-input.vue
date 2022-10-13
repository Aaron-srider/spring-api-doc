<template>
    <div>
        <!-- #region 重命名不生效时 -->
        <div v-if="!if_rename" class="ellipsis-text">
            <slot :text="text">
                <span class="fs16">
                    {{ text }}
                </span>
            </slot>
        </div>
        <!-- #endregion -->
        <!-- #region 重命名时 -->
        <div v-else class="flex rename-container">
            <!-- 重命名 input -->
            <div>
                <el-input :value="new_name" @input="handle_input"></el-input>
            </div>
            <!-- 确认重命名 btn -->
            <div class="confirm">
                <el-button @click="rename()">
                    <i class="el-icon-check"></i>
                </el-button>
            </div>
            <!-- 取消重命名 btn-->
            <div>
                <el-button @click="close_rename()">
                    <i class="el-icon-close"></i>
                </el-button>
            </div>
        </div>
        <!-- #endregion -->
    </div>
</template>

<script>
export default {
    data() {
        return {};
    },
    methods: {
        rename() {
            this.$emit('confirm');
        },
        close_rename() {
            this.$emit('close_rename');
        },
        handle_input(event) {
            this.$emit('input', event);
        },
    },
    props: {
        if_rename: {
            required: true,
            type: Boolean,
        },
        text: {
            required: true,
            type: String,
        },
        new_name: {
            required: true,
            type: String,
        },
    },
};
</script>

<style lang="scss" scoped>
@import '~@/styles/index.scss';
</style>
>
