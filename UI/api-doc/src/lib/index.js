import {ArrayUtils} from '@/utils';
import {Message} from 'element-ui';

export function fetch_options_to_this(options) {
    var keys = Object.keys(options);
    keys.forEach((key) => {
        var value = options[key];
        this[key] = value;
    });
}

function check_option_key(classname, options, target_key, key_type, present) {
    var optionKeys = Object.keys(options);
    let key = ArrayUtils.getFirst(optionKeys, (key) => key === target_key);

    let present_correct = false;
    if (present === true) {
        present_correct = key != undefined;
    } else {
        present_correct = key == undefined;
    }

    if (!present_correct) {
        let msg = `${classname}对象${
            present ? '缺少' : '不能覆盖'
        }${target_key}${key_type === 'function' ? '函数' : '变量'}`;
        throw new Error(msg);
    }

    if (present) {
        let key_type_correct = false;
        if (key_type === 'function') {
            key_type_correct = typeof options[key] === 'function';
        } else if (key_type === 'var') {
            key_type_correct = typeof options[key] !== 'function';
        }

        if (!key_type_correct) {
            let msg = `ElDialog对象${
                present ? '缺少' : '不能覆盖'
            }${target_key}${key_type === 'function' ? '函数' : '变量'}`;
            throw new Error(msg);
        }
    }
}

function build_parent_ref(options, parent) {
    var keys = Object.keys(options);
    keys.map((key) => {
        var value = options[key];
        return value;
    })
        .filter((option) => {
            return option instanceof OopElComponent;
        })
        .forEach((option) => {
            option.parent = parent;
        });
}

class OopElComponent {
}

function uuidv4() {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
        (
            c ^
            (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
        ).toString(16),
    );
}

export class OopElDialogModel extends OopElComponent {
    constructor(title, options) {
        super();
        this.title = title;
        this.visible = false;

        build_parent_ref(options, this);

        check_option_key(
            'OopElDialogModel',
            options,
            'reset_data',
            'function',
            true,
        );

        check_option_key(
            'OopElDialogModel',
            options,
            'before_open',
            'function',
            true,
        );
        //
        check_option_key(
            'OopElDialogModel',
            options,
            'open',
            'function',
            false,
        );
        check_option_key(
            'OopElDialogModel',
            options,
            'commit',
            'function',
            false,
        );
        check_option_key(
            'OopElDialogModel',
            options,
            'do_open',
            'function',
            false,
        );
        check_option_key(
            'OopElDialogModel',
            options,
            'close',
            'function',
            false,
        );

        fetch_options_to_this.call(this, options);
        this.reset_data();

        this.key_down_listener_count = 0;
        this.event_map = new Map();
        this.id = uuidv4();
    }

    open(data, $event) {
        if ($event != undefined) {
            let target = $event.currentTarget;
            target.blur();
        }

        let enter_commmit_event = this.enter_down();
        this.event_map.set('keydown', enter_commmit_event);
        // 绑定监听事件
        window.addEventListener('keydown', enter_commmit_event);

        this.before_open(data);
    }

    commit(data) {
        var enable_commit = this.enable_commit(data);
        if (enable_commit === true) {
            this.do_commit(data);
        } else {
            Message.warning('请将表单填写完整后提交');
        }
    }

    do_commit(data) {
    }

    enable_commit(data) {
        return true;
    }

    topmost() {
        let el_dialog_wrappers =
            document.getElementsByClassName('el-dialog__wrapper');
        let topmost_id;
        let topmost_value = 0;

        for (let i = 0; i < el_dialog_wrappers.length; i++) {
            const dialog_wrapper = el_dialog_wrappers[i];
            let display = $(dialog_wrapper).css('display');
            if (display !== 'none') {
                let zindex = $(dialog_wrapper).css('z-index');
                if (zindex > topmost_value) {
                    topmost_value = zindex;
                    topmost_id = $(dialog_wrapper).attr('id');
                }
            }
        }
        return topmost_id === this.id;
    }

    enter_down() {
        return (e) => {
            // 回车则执行登录方法 enter键的ASCII是13
            if (e.keyCode === 13) {
                // debugger;
                if (this.visible) {
                    if (this.topmost()) {
                        this.commit();
                    }
                }
            }
            e.stopPropagation();
        };
    }

    before_open(data) {
        this.do_open();
    }

    do_open() {
        this.visible = true;
    }

    // 重置data为默认的结构，可以复写这个方法
    reset_data(data) {
        this.data = {};
    }

    // 关闭表单清除信息
    close() {
        window.removeEventListener('keydown', this.event_map.get('keydown'));

        this.before_close();
        this.visible = false;
        this.reset_data();
        recursive_reset_data(this);
    }

    before_close() {
    }
}

function isPrimitiveValue(value) {
    if (
        typeof value === 'string' ||
        typeof value === 'number' ||
        value == null ||
        typeof value === 'boolean' ||
        Number.isNaN(value)
    ) {
        return true;
    }

    return false;
}

function recursive_reset_data(obj) {
    var keys = Object.keys(obj);
    keys.forEach((attr_name, idx) => {
        if (attr_name === 'parent') {
            return;
        }
        let attr_value = obj[attr_name];
        if (isPrimitiveValue(attr_value)) {
            return;
        }
        if (
            attr_value.hasOwnProperty('reset_data') &&
            typeof attr_value['reset_data'] === 'function'
        ) {
            attr_value['reset_data']();
            return;
        }
        recursive_reset_data(attr_value);
    });
}

export class OopElSelectModel extends OopElComponent {
    constructor(options) {
        super();
        build_parent_ref(options, this);
        fetch_options_to_this.call(this, options);
        this.reset_data();
        this.value = '';
        this.id = uuidv4();
        this.loading = false;
    }

    reset_data() {
        this.data = {};
        this.value = '';
    }
}

export class OopElTableModel extends OopElComponent {
    constructor(title, options) {
        super();
        this.data = [];
        this.title = title;
        this.loading = false;
        this.cols = [
            {prop: 'col1_prop_name', label: 'col1_label_name'},
            {prop: 'col2_prop_name', label: 'col2_label_name'},
        ];

        build_parent_ref(options, this);

        check_option_key('OopElTableModel', options, 'cols', 'var', true);
        check_option_key(
            'OopElTableModel',
            options,
            'do_fetch_data',
            'function',
            true,
        );
        //
        check_option_key(
            'OopElTableModel',
            options,
            'fetch_data',
            'function',
            false,
        );
        check_option_key(
            'OopElTableModel',
            options,
            'fetch_over',
            'function',
            false,
        );
        fetch_options_to_this.call(this, options);
    }

    fetch_data(data) {
        console.log(`表格 ${this.title} 开始拉取数据`);
        this.loading = true;
        this.do_fetch_data(data);
    }

    fetch_over() {
        this.loading = false;
    }

    start_loading() {
        this.loading = true;
    }

    // 可以重写
    do_fetch_data(data) {
        this.data = [];
    }
}

export class OopElAutocompletionModel extends OopElComponent {
    constructor(placeholder, options) {
        super();
        build_parent_ref(options, this);
        check_option_key(
            'OopElAutocompletionModel',
            options,
            'reset_data',
            'function',
            true,
        );
        fetch_options_to_this.call(this, options);
        this.placeholder = placeholder;
        this.reset_data();
        this.selected = {};
        this.value = '';
    }

    fetch_suggestions(query_string, cb) {
        cb(['value1', 'value2', 'value3']);
    }

    reset_data() {
    }

    do_select(row) {
        console.log('select row: ', row);
    }
}

// 与MyElPagination匹配的对象，记录分页信息，负责分页操作
export class OopElPaginationModel extends OopElComponent {
    constructor(pageno, pagesize, options) {
        super();
        this.total = 0;
        this.pageno = pageno;
        this.pagesize = pagesize;

        build_parent_ref(options, this);
        fetch_options_to_this.call(this, options);
    }

    // 用户覆盖该方法，实现翻页
    do_page(data) {
    }
}
