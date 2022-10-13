import axios from 'axios';
import {Message} from 'element-ui';
import store from '@/store';
import router from '@/router';
import {get_token, token_err_code,} from '@/utils/auth';
import {USER_RESET_USER_STATE} from '@/store/modules/user';

// create an axios instance
const service = axios.create({
    baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
    // withCredentials: true, // send cookies when cross-domain requests
    timeout: 5000, // request timeout
});

// request interceptor
service.interceptors.request.use(
    (config) => {
        // do something before request is sent
        console.log('发起请求：', config);

        return config;
    },
    (error) => {
        // do something with request error
        console.log(error); // for debug
        return Promise.reject(error);
    },
);

export function resp_success(code) {
    if (code != undefined) {
        return code == 'SUCCESS';
    }
    return false;
}

// response interceptor
service.interceptors.response.use(
    /**
     * If you want to get http information such as headers or status
     * Please return  response => response
     */

    /**
     * Determine the request status by custom code
     * Here is just an example
     * You can also judge the status by HTTP Status Code
     */
    (response) => {
        const res = response.data;

        var code = res.code;

        // 请求成功，放回请求到then中
        if (resp_success(code)) {
            console.log('请求成功，返回体：', res);
            return res;
        }

        // 统一处理重复请求
        if (code === 'REPEAT_REQUEST') {
            console.log('错误码：', code, '重复请求');
            Message.warning('请勿重复请求');
            return Promise.reject();
        }

        // 统一处理权限不足
        if (code === 'PERMISSION_NOT_REQUIRED') {
            console.log('错误码：', code, ' 无权进行此操作');
            Message.warning('您无权进行此操作');
            return Promise.reject();
        }

        // 统一处理token验证无用户异常
        if (code === 'AUTH_USER_NOT_EXISTS') {
            console.log('错误码：', code, ' 无用户');
            Message.warning('用户失效,请重新登录');
            return Promise.reject(res);
        }

        // 统一处理token错误请求
        if (code === 'TOKEN_JWT_EXPIRED') {
            console.log('token：', get_token(), ' 失效，跳转到登录页面');
            Message.warning('您的登录信息已失效，请重新登录');
            store.commit(USER_RESET_USER_STATE);
            router.push('/login');
            return Promise.reject();
        }

        if (token_err_code(code)) {
            console.log('token错误，错误码：', code, ' 跳转到登录页面');
            Message.error('您的身份信息有误，请重新登录');
            store.commit(USER_RESET_USER_STATE);
            router.push('/login');
            return Promise.reject();
        }

        // 如果idemtoken还没请求下来用户就操作，提示用户操作太快
        if (code === 'IDEM_TOKEM_MISSING') {
            console.log('错误码：', code, ' 请求缺少idem-token');
            Message.warning('您的操作太快');
            return Promise.reject();
        }

        // 其他错误，进catch中处理
        console.log('请求异常，返回体', res);
        return Promise.reject(res);
    },
    (error) => {
        // 统一处理网络错误
        Message({
            message: '无法连接服务器',
            type: 'error',
            duration: 2 * 1000,
        });
        return Promise.reject(error);
    },
);

export default service;
