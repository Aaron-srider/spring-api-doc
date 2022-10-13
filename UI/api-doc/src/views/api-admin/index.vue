<template>
    <div>
        <page-header :title="page_title"></page-header>
        <div class="" style="width: 95%; margin: 20px auto 0 auto">
            <!-- 模块 -->
            <div
                class="mgt20"
                v-for="api_module in modules"
                :key="api_module.id"
            >
                <!-- 模块名 -->
                <div
                    class="module-title pdb10"
                    @click="switch_show(api_module)"
                >
                    {{ api_module.name }}
                </div>

                <!-- 模块内容 -->
                <div
                    v-show="if_show(api_module)"
                    :id="`api_module-${api_module.id}`"
                >
                    <!-- 接口 -->
                    <div
                        :class="`mgt20`"
                        v-for="api in api_module.apis"
                        :key="api.id"
                    >
                        <!-- 接口标题 -->
                        <div
                            @click="switch_show(api)"
                            :class="`flex mgt10 api-title ${api_title_css(
                                api.method,
                            )}`"
                        >
                            <div class="flexg5 pdt20 pdl20">
                                {{ api.name }}
                            </div>
                            <div class="flex flexg5 pd15 pdl20">
                                <div>
                                    <div
                                        :class="`${api_method_tag_css(
                                            api.method,
                                        )}`"
                                    >
                                        {{ api.method }}
                                    </div>
                                </div>
                                <div
                                    class="pdt5 mgl20"
                                    style="
                                        font-family: Source Code Pro, monospace;
                                    "
                                >
                                    {{ api.url }}
                                </div>
                            </div>
                        </div>

                        <!-- 接口信息 -->
                        <div v-show="if_show(api)">
                            <!-- 接口说明 -->
                            <div class="mgt10">
                                <div class="section-title">接口说明</div>
                                <div>
                                    {{ display_or_empty(api.detail, '无') }}
                                </div>
                            </div>

                            <!-- 请求参数 -->
                            <div class="mgt10">
                                <el-table
                                    :data="api.params"
                                    border
                                    style="width: 100%"
                                >
                                    <el-table-column
                                        prop="name"
                                        label="字段名"
                                    ></el-table-column>
                                    <el-table-column
                                        prop="param_type"
                                        label="参数类型"
                                    ></el-table-column>
                                    <el-table-column
                                        prop="data_type"
                                        label="字段类型"
                                    ></el-table-column>
                                    <el-table-column
                                        label="是否必填"
                                    >
                                        <template slot-scope="scope">
                                            {{scope.row.required ===true ? "是": "否"}}
                                        </template>
                                    </el-table-column>

                                    <el-table-column
                                        prop="default"
                                        label="默认值"
                                    ></el-table-column>
                                    <el-table-column
                                        prop="detail"
                                        label="说明"
                                    ></el-table-column>
                                    <el-table-column
                                        prop="example"
                                        label="Example"
                                        width="400"
                                    >
                                        <template slot-scope="scope">
                                            <div
                                                @click="
                                                    if_show_example(scope.row)
                                                "
                                            >
                                                <v-md-preview
                                                    :id="`param-example-${scope.row.id}`"
                                                    :text="`\`\`\` json\n ${
                                                        scope.row
                                                            .show_example ===
                                                        true
                                                            ? scope.row.example
                                                            : 'expand'
                                                    } \n \`\`\``"
                                                    style="cursor: pointer"
                                                ></v-md-preview>
                                            </div>
                                        </template>
                                    </el-table-column>
                                </el-table>
                            </div>

                            <!-- 请求参数额外说明 -->
                            <div>
                                <div class="section-title">
                                    请求参数特别说明
                                </div>
                                <div>
                                    {{
                                        display_or_empty(
                                            api.param_extra_info,
                                            '无',
                                        )
                                    }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { OopElTableModel } from '@/lib';
import { get_api_modules } from '@/api/api_doc';
import pageHeader from '@/views/common/page-header.vue';

var vue;
export default {
    components: { pageHeader },

    data() {
        return {
            page_title: '接口文档',
            table: new OopElTableModel('权限表格', {
                cols: [
                    {
                        prop: 'name',
                        label: '字段名',
                    },
                    {
                        prop: 'type',
                        label: '类型',
                    },
                    {
                        prop: 'required',
                        label: '必输',
                    },
                    {
                        prop: 'default',
                        label: '默认值',
                    },
                    {
                        prop: 'detail',
                        label: '说明',
                    },
                ],

                do_fetch_data(data) {
                    this.data = [];
                },
            }),
            modules: [
                {
                    id: 1,
                    name: '测试模块',
                    apis: [
                        {
                            id: 1,
                            name: '登录接口',
                            method: 'GET',
                            url: '/auth_user',
                            detail: '用于登录',
                            params: [
                                {
                                    name: 'size',
                                    type: 'int',
                                    required: false,
                                    default: '10',
                                    detail: '分页大小',
                                },
                                {
                                    name: 'current',
                                    type: 'int',
                                    required: false,
                                    default: '10',
                                    detail: '分页大小',
                                },
                            ],
                            responses: [
                                {
                                    code: 'SUCCESS',
                                    msg: '成功',
                                },
                            ],
                            param_extra_info: '分页大小默认10',
                        },
                        {
                            id: 2,
                            name: '修改密码接口',
                            method: 'POST',
                            detail: '用于修改密码接口',
                            param_extra_info: '',
                            url: '/passwd',
                        },
                        {
                            id: 3,
                            name: '修改设备接口',
                            method: 'DELETE',
                            param_extra_info: '',
                            detail: '用于修改设备接口',
                            url: '/user',
                        },
                        {
                            id: 4,
                            name: '新增用户接口',
                            method: 'PUT',
                            param_extra_info: '',
                            url: '/insert_user',
                            detail: '用于登录',
                        },
                    ],
                },
            ],
        };
    },
    methods: {
        fetch_data() {
            get_api_modules().then((resp) => {
                let modules = resp.data.modules;

                for (let i = 0; i < modules.length; i++) {
                    let module = modules[i];
                    module.show = false;
                    let apis = module.apis;
                    for (let j = 0; j < apis.length; j++) {
                        let api = apis[j];
                        api.show = false;

                        let params = api.params;
                        for (let k = 0; k < params.length; k++) {
                            const param = params[k];
                            let example = param.example;
                            param.example = JSON.stringify(
                                JSON.parse(example),
                                null,
                                4,
                            );
                            if (
                                param.example === 'null' ||
                                param.example == undefined
                            ) {
                                param.show_example = true;
                            } else {
                                param.show_example = false;
                            }
                        }
                    }
                }
                this.modules = modules;
            });
        },

        api_title_css(method) {
            return method === 'GET'
                ? 'get-method-banner'
                : method === 'POST'
                ? 'post-method-banner'
                : method === 'PUT'
                ? 'put-method-banner'
                : method === 'DELETE'
                ? 'delete-method-banner'
                : '';
        },
        api_method_tag_css(method) {
            return method === 'GET'
                ? 'get-method-tag'
                : method === 'POST'
                ? 'post-method-tag'
                : method === 'PUT'
                ? 'put-method-tag'
                : method === 'DELETE'
                ? 'delete-method-tag'
                : '';
        },
        method_tag_type(method) {
            return method === 'GET'
                ? 'success'
                : method === 'POST'
                ? ''
                : method === 'PUT'
                ? 'warning'
                : method === 'DELETE'
                ? 'danger'
                : '';
        },
        if_show(item) {
            if (item.show == undefined || item.show === false) {
                return false;
            } else {
                return true;
            }
        },

        switch_show(item) {
            if (item.show == undefined || item.show === false) {
                item.show = true;
            } else {
                item.show = false;
            }
        },

        display_or_empty(str, defalut) {
            return str === '' || str == undefined ? defalut : str;
        },
        // TODO
        if_show_example(param) {
            if (
                param.show_example != undefined &&
                param.show_example === true
            ) {
                param.show_example = false;
                return;
            }
            param.show_example = true;
        },
    },
    created() {
        vue = this;

        this.fetch_data();
    },
};
</script>

<style lang="scss">
@import '~@/styles/common-style.scss';
@import '~@/views/api-admin/css.scss';
</style>
