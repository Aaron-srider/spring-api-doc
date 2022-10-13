import request from '@/utils/request';

export function get_api_modules() {
    return request({
        url: `/modules`,
        method: 'get',
    });
}
