<template>
    <div>
        {{ display_value }}
    </div>
</template>

<script>
import { ArrayUtils, get_display_time, get_display_file_size } from '@/utils';
export default {
    computed: {
        display_value() {
            switch (this.type) {
                case 'time':
                    return this.get_display_time(new Date(this.value));
                case 'duration':
                    return this.get_display_duration(this.value);
                case 'file-size':
                    return this.get_display_file_size(this.value);
                default:
                    return '';
            }
        },
    },
    methods: {
        get_display_duration(msTime) {
            let time = msTime / 1000;

            let hour = Math.floor(time / 60 / 60);

            hour = hour.toString().padStart(2, '0');

            let minute = Math.floor(time / 60) % 60;

            minute = minute.toString().padStart(2, '0');

            let second = Math.floor(time) % 60;

            second = second.toString().padStart(2, '0');

            return `${hour}:${minute}:${second}`;
        },
        get_display_time(updateTime) {
            if (updateTime === null) {
                return '';
            }

            let now = new Date().getTime();
            let second = Math.floor((now - updateTime) / 1000);
            let minute = Math.floor(second / 60);
            let hour = Math.floor(minute / 60);
            let day = Math.floor(hour / 24);
            let month = Math.floor(day / 31);
            let year = Math.floor(month / 12);

            if (year > 0) {
                return year + '年前';
            } else if (month > 0) {
                return month + '月前';
            } else if (day > 0) {
                let ret = day + '天前';
                if (day >= 7 && day < 14) {
                    ret = '1周前';
                } else if (day >= 14 && day < 21) {
                    ret = '2周前';
                } else if (day >= 21 && day < 28) {
                    ret = '3周前';
                } else if (day >= 28 && day < 31) {
                    ret = '4周前';
                }
                return ret;
            } else if (hour > 0) {
                return hour + '小时前';
            } else if (minute > 0) {
                return minute + '分钟前';
            } else if (second > 0) {
                return second + '秒前';
            } else {
                return '刚刚';
            }
        },
        get_display_file_size(bytes) {
            let g = bytes / 1024.0 / 1024.0 / 1024.0;
            if (g >= 1) {
                return `${g.toFixed(2)} G`;
            }
            let m = bytes / 1024.0 / 1024.0;
            if (m >= 1) {
                return `${m.toFixed(2)} M`;
            }
            let k = bytes / 1024.0;
            if (k >= 1) {
                return `${k.toFixed(2)} k`;
            }
            let b = bytes;
            if (b >= 1) {
                return `${b.toFixed(2)} b`;
            }
            return '0 b';
        },
    },
    created() {
        switch (this.type) {
            case 'time':
                this.display_value = get_display_time(new Date(this.value));
                break;

            case 'file-size':
                this.display_value = get_display_file_size(this.value);
                break;
        }
    },
    data() {
        return {};
    },
    props: {
        value: '',
        type: '',
    },
};
</script>
