<template>
    <div>
        <div @click="back()" class="pointer back-btn">
            <slot>
                <i class="el-icon-back"></i>
            </slot>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        path_option: {
            required: true,
            type: Object,
        },
    },
    methods: {
        back() {
            this.$router.push(this.path_option);
        },
    },
};
</script>

<style lang="scss">
@import '~@/styles/common-style.scss';
.back-btn {
    font-size: 30px;
    &:hover {
        color: $google-blue;
    }
}
</style>
