<template>
    <!-- 修改 对话框 -->
    <el-dialog
        :id="model.id"
        :title="model.title"
        :visible.sync="model.visible"
        :width="width"
        :close-on-click-modal="false"
        @close="handle_close"
    >
        <slot :data="model.data" :model="model"></slot>

        <span v-show="with_commit" slot="footer" class="dialog-footer">
            <slot name="footer" :data="model.data" :model="model">
                <div>
                    <el-button @click="model.close()">取消</el-button>
                    <el-button @click="model.commit()" type="primary">
                        提交
                    </el-button>
                </div>
            </slot>
        </span>
    </el-dialog>
</template>

<script>
import { OopElDialogModel } from '@/lib/index';
export default {
    props: {
        model: OopElDialogModel,
        width: {
            default: '30%',
        },
        with_commit: {
            default: true,
        },
    },
    methods: {
        handle_close() {
            this.model.close();
        },
    },
};
</script>

<style></style>
