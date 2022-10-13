import {get_login_info, get_token, removeToken, set_login_info, set_token,} from '@/utils/auth';

const getDefaultState = () => {
    return {
        token: get_token(),
        login_info: get_login_info(),
    };
};

const state = getDefaultState();

export const USER_RESET_USER_STATE = 'user/reset_user_state';
export const USER_SET_TOKEN = 'user/set_token';
export const USER_SET_LOGIN_INFO = 'user/set_login_info';
export const USER_LOG_OUT = 'user/log_out';

const mutations = {
    set_login_info: (state, login_info) => {
        state.login_info = login_info;
        set_login_info(login_info);
    },

    set_token: (state, token) => {
        state.token = token;
        set_token(token);
    },

    reset_user_state: (state) => {
        state.token = undefined;
        state.login_info = undefined;
        set_token(undefined);
        set_login_info(undefined);
    },
};

const actions = {
    // user login
    login({commit}, userInfo) {
        return new Promise((resolve, reject) => {

        });
    },

    // get user info
    getInfo({commit, state}) {
        return new Promise((resolve, reject) => {

        });
    },

    // user logout
    logout({commit, state}) {
        return new Promise((resolve, reject) => {

        });
    },

    // remove token
    resetToken({commit}) {
        return new Promise((resolve) => {
            removeToken(); // must remove  token  first
            commit('RESET_STATE');
            resolve();
        });
    },
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
};
