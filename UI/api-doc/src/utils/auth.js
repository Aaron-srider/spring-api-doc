import Cookies from 'js-cookie';
import store from '@/store';
import {USER_RESET_USER_STATE} from '@/store/modules/user';
import {Message} from 'element-ui';

const TokenKey = 'vue_admin_template_token';

const login_info_key = 'login_info_key';

const token_key = 'token_key';

export function getToken() {
    var login_info = get_login_info();
    if (login_info) {
        return login_info.token;
    }
    return null;
}

export function setToken(token) {
    return Cookies.set(TokenKey, token);
}

export function removeToken() {
    return Cookies.remove(TokenKey);
}

export function get_login_info() {
    var user_info_json = Cookies.get(login_info_key);
    // 由于存入时会使用stringfy，所以undefined也会字符串化
    if (user_info_json == 'undefined') {
        user_info_json = undefined;
    }
    if (user_info_json != undefined) {
        return JSON.parse(Cookies.get(login_info_key));
    }
    return undefined;
}

export function set_login_info(login_info) {
    return Cookies.set(login_info_key, JSON.stringify(login_info));
}

export function get_token() {
    var value = Cookies.get(token_key);
    if (value == 'undefined') {
        value = undefined;
    }
    return value;
}

export function set_token(token) {
    return Cookies.set(token_key, token);
}

export function token_err_code(code) {
    var token_error_list = {
        10040: '0',
        10041: '1',
        10042: '2',
        10043: '3',
        10044: '4',
        10045: '5',
        10046: '6',
    };

    if (token_error_list[code] == undefined) {
        return false;
    }
    return true;
}

export function get_idem() {
    return new Promise((resolve, reject) => {
        get_idempotent_token(get_token()).then((resp) => {
            if (resp.code != undefined && resp.code == 'SUCCESS') {
                console.log('get idem token: ', resp.data.idemToken);
                resolve(resp.data.idemToken);
            }
            reject(resp.code);
        });
    });
}

// =========================== auth_user_menu function utils ========================
function pre_left_match(menu_array, path) {
    return menu_array.findIndex((elem) => {
        var elem_path = elem.path;
        var index_of_path = elem_path.indexOf(path);
        let elem_path_len = elem_path.length;
        let path_len = path.length;

        if (elem_path_len < path_len) {
            return false;
        }

        if (path === elem_path) {
            return true;
        }
        if (elem_path_len === path_len) {
            return false;
        }

        let next_char = elem.path.substring(path_len, path_len + 1);
        if (index_of_path === 0 && next_char === '/') {
            return true;
        }
        return false;
    });
}

// =========================== auth_user_menu function utils ========================

export var auth_user_menu = function (user_allow_menus) {
    if (user_allow_menus == undefined || user_allow_menus.length === 0) {
        // 用户菜单中没有数据，那么不用校验，直接隐藏所有需要权限校验的菜单
        return function (node_idx, node, path, parent_node, flag) {
            // 检查该页面是否需要校验权限
            var auth = node.auth;
            // 如果没有auth属性，默认需要鉴权，如果auth为true，也需要校验权限
            if (auth == undefined || (auth != undefined && auth === true)) {
                // 用户无权访问该菜单，不渲染该菜单及其所有孩子
                console.log('用户' + '无权访问菜单:' + path + '，不渲染该菜单');
                // 从双亲中删除当前节点
                parent_node.splice(node_idx, 1);
                flag.if_continue = true;
                // 跳过所有孩子
                flag.visit_children = false;
                return;
            } else {
                // 不需要鉴权，则所有孩子都不检查，跳过所有孩子
                flag.visit_children = false;
                return;
            }
        };
    }

    return function (node_idx, node, path, parent_node, flag) {
        // 检查该页面是否需要校验权限
        var auth = node.auth;
        // 如果没有auth属性，默认需要鉴权，如果auth为true，也需要校验权限
        if (auth == undefined || (auth != undefined && auth === true)) {
            // 需要鉴权
            var find_menu_idx = pre_left_match(user_allow_menus, path);
            if (find_menu_idx === -1) {
                // 用户无权访问该菜单，不渲染该菜单及其所有孩子
                console.log('用户' + '无权访问菜单:' + path + '，不渲染该菜单');
                // 从双亲中删除当前节点
                parent_node.splice(node_idx, 1);
                flag.if_continue = true;
                // 跳过所有孩子
                flag.visit_children = false;
                return;
            } else {
                // 用户有权访问该菜单，不删除该菜单
                console.log('用户' + '有权访问菜单:' + path + '，渲染该菜单');
                flag.visit_children = true;
                return;
            }
        } else {
            // 不需要鉴权，则所有孩子都不检查，跳过所有孩子
            flag.visit_children = false;
            return;
        }
    };
};

/**
 * token 错误时，输出一些信息，并清楚用户信息
 */
export function when_token_err(callback) {
    Message.warning('您的登录信息已失效，请重新登录');
    store.commit(USER_RESET_USER_STATE);
    console.log('token：', token, ' \n 无效，需要重新验证');
    callback();
}
