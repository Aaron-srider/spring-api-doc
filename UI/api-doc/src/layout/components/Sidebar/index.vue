<template>
    <div :class="{ 'has-logo': showLogo }">
        <logo v-if="showLogo" :collapse="isCollapse"></logo>
        <div class="sidebar-info flex">
            <div>
            <!--<img class="avator" :src="default_user_avator" alt="" />-->
            </div>
            <div style="padding: 4px">
                <div class="account mgb8 mgl10">{{ user.samAccountName }}</div>
                <div class="flex">
                    <div
                        :key="role.name"
                        v-for="role in user.roles"
                        class="role mgl10"
                    >
                        {{ role.name }}
                    </div>
                </div>
            </div>
        </div>
        <el-scrollbar
            wrap-class="scrollbar-wrapper"
            style="border-right: 1px solid #e8e8e8"
        >
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="variables.menuBg"
                :text-color="variables.menuText"
                :unique-opened="false"
                :active-text-color="variables.menuActiveText"
                :collapse-transition="false"
                mode="vertical"
            >
                <sidebar-item
                    v-for="route in routes"
                    :key="route.path"
                    :item="route"
                    :base-path="route.path"
                ></sidebar-item>
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<style lang="scss">
@import '~@/styles/common-style.scss';

.sidebar-info {
    padding: 10px;
    height: 60px;
    font-size: 13px;
    background-color: $google-gray-100;
    border-top: 1px solid $google-gray-300;
    border-right: 1px solid $google-gray-300;
    border-bottom: 1px solid $google-gray-300;
}

.account {
}
.role {
    font-size: 12px;
}
.avator {
    width: 40px;
    height: 40px;
    border-radius: 20px;
}
</style>

<script>
import default_user_avator from '@/assets/default_user_avator.jpg';
import { mapGetters } from 'vuex';
import Logo from './Logo';
import SidebarItem from './SidebarItem';
import variables from '@/styles/variables.scss';
import { get_sidebar_routes } from '@/router';

export default {
    components: { SidebarItem, Logo },
    data() {
        return {
            default_user_avator: default_user_avator,
            user: {
                samAccountName: '',
                roles: [
                    {
                        name: '',
                    },
                ],
            },
        };
    },
    created() {
        // this.user = this.$store.state.user.login_info;
    },
    computed: {
        ...mapGetters(['sidebar']),
        routes() {
            return get_sidebar_routes();
            // return this.$router.options.routes;
        },
        activeMenu() {
            const route = this.$route;
            const { meta, path } = route;
            // if set path, the sidebar will highlight the path you set
            if (meta.activeMenu) {
                return meta.activeMenu;
            }
            return path;
        },
        showLogo() {
            return this.$store.state.settings.sidebarLogo;
        },
        variables() {
            return variables;
        },
        isCollapse() {
            // return !this.sidebar.opened;
            return !this.sidebar.opened;
        },
    },
};
</script>
