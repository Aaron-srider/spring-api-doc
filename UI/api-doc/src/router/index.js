import Vue from 'vue';
import Router from 'vue-router';
import {cloneDeep} from '@/utils/route';
/* Layout */
import Layout from '@/layout';

Vue.use(Router);

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */

export let srcRoutes = [

    {
        path: '/404',
        component: () => import('@/views/404'),
        auth: false,
        hidden: true,
    },

    {
        path: '/',
        component: Layout,
        auth: false,
        redirect: '/api-admin',
    },

    {
        path: '/api-admin',
        component: Layout,
        redirect: '/api-admin/index',
        name: 'api-admin',
        alwaysShow: false,
        meta: {title: '', icon: 'ali-international-icon-template'},
        children: [
            {
                path: 'index',
                name: 'api-admin_index',
                component: () => import('@/views/api-admin/index'),
                meta: {
                    title: '接口文档',
                    icon: 'ali-international-icon-template',
                },
            },
        ],
    },

    // 404 page must be placed at the end !!!

    {path: '*', redirect: '/404', hidden: true, auth: false},
];

// export var routes = cloneDeep(srcConstantRoutes);
export let routes = srcRoutes;
let copy_routes_for_sidebar = cloneDeep(srcRoutes);

export function reset_sidebar_routes() {
    copy_routes_for_sidebar = cloneDeep(srcRoutes);
}

export function clean_sidebar_routes() {
    copy_routes_for_sidebar = [
        {path: '*', redirect: '/404', hidden: true, auth: false},
    ];
}

export function get_sidebar_routes() {
    return copy_routes_for_sidebar;
}

export {copy_routes_for_sidebar};

const createRouter = () =>
    new Router({
        mode: 'history', // require service support
        scrollBehavior: () => ({y: 0}),
        routes: routes,
    });

const router = createRouter();

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
    const newRouter = createRouter();
    router.matcher = newRouter.matcher; // reset router
}

export default router;
