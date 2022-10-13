import {copy_routes_for_sidebar} from '@/router';
import {auth_user_menu} from './auth';
import {traverse_tree} from './route';
import {Message} from 'element-ui';

export function resolve_ou_list(ou_list_json) {
    // json转换成字符串列表
    var ou_str_list = JSON.parse(ou_list_json);
    console.log('解析json格式的ou_dn_list：', ou_list_json, '=>', ou_list);

    var ou_list = [];
    // ou_path_str格式：ou=xx, ou=xx, dc=xx, dc=xx
    for (var ou_path_str of ou_str_list) {
        var ou_path_array = resolve_ou_path(ou_path_str);
        console.log('解析ou_path：', ou_path_str, '=>', ou_path_array);

        ou_list.push(ou_path_array);
    }
    return ou_list;
}

function resolve_ou_path(ou_path_str) {
    console.log('开始解析ou_path:', ou_path_str);
    var ou_path = [];
    var dn_entrys = ou_path_str.split(',');
    console.log('获取entrys:', dn_entrys);
    for (var entry of dn_entrys) {
        console.log('解析entry:', entry);
        var key = get_entry_key(entry, '=');
        var value = get_entry_value(entry, '=');
        if (key == 'ou') {
            ou_path.push(value);
        }
    }
    return ou_path.reverse();
}

function get_entry_key(entry, split) {
    var key_value = entry.split(split);
    var key = key_value[0];
    if (key != undefined) {
        return key.trim();
    }
    return undefined;
}

function get_entry_value(entry, split) {
    var key_value = entry.split(split);
    var value = key_value[1];
    if (value != undefined) {
        return value.trim();
    }
    return undefined;
}

// ou_list形如：['研发部','软件研发部']，str形如："研发部，软件研发部"
export function parse_ou_list_to_str(ou_list) {
    var one_ou_str = '';
    for (var i = 0; i < ou_list.length; i++) {
        one_ou_str += ou_list[i];
        if (i != ou_list.length - 1) {
            one_ou_str += ', ';
        }
    }
    return one_ou_str;
}

// group_metrix形如： [['研发部','软件研发部'],['测试部','测试开发组']]，string list形如：["研发部，软件研发部", "测试部，测试开发组"]
export function parse_user_group_metrix_to_string_list(user_group_metrix) {
    var user_group_str_list = [];
    for (var i = 0; i < user_group_metrix.length; i++) {
        var one_group_str = parse_ou_list_to_str(user_group_metrix[i]);
        user_group_str_list.push(one_group_str);
    }
    return user_group_str_list;
}

export function process_user_menu(
    login_info,
    menus_empty_callback,
    done_callback,
) {
    // 鉴别用户是否是超级管理员
    var is_super_admin = false;
    var roles = login_info.roles;
    if (roles != undefined && roles.length != null) {
        var super_admin_idx = roles.findIndex(
            (elem) => elem.name === '超级管理员',
        );
        if (super_admin_idx != -1) {
            is_super_admin = true;
        }
    }

    // 超级管理员不用过滤菜单
    if (is_super_admin != undefined && is_super_admin === true) {
        console.log('用户是超级管理员，开放所有目录');
        done_callback();
        return true;
    }

    // 用户不是超级管理员，处理并过滤用户允许访问的菜单
    var user_allow_menus = login_info.menus;
    if (user_allow_menus == undefined) {
        Message.warning('用户数据错误');
        menus_empty_callback();
        return false;
    }

    // 用户信息存在，过滤一遍菜单
    console.log(`用户`, login_info, `有权访问的菜单：`, user_allow_menus);
    var op = auth_user_menu(user_allow_menus);
    traverse_tree(copy_routes_for_sidebar, '', op);
    done_callback();
    return true;
}

export function parse_user_info_response(user_info_resp) {
    return {
        roles: user_info_resp.roles,
        cn: user_info_resp.cn,
        dn: user_info_resp.dn,
        mail: user_info_resp.dn,
        ouPathList: user_info_resp.ouPathList,
        samAccountName: user_info_resp.samAccountName,
        uid: user_info_resp.uid,
        menus: user_info_resp.menus,
    };
}
