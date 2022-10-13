// =========================== traverse_tree function utils ========================
function get_child_absolute_path(child_relative_path, parent_absolute_path) {
    var child_path = parent_absolute_path;
    var parent_path_last_char = '';
    if (parent_absolute_path.length != 0) {
        parent_path_last_char = parent_absolute_path.substring(
            parent_absolute_path.length - 1,
            parent_absolute_path.length,
        );
    } else {
        parent_path_last_char = '';
    }

    var child_path_first_char = '';
    child_path_first_char = child_relative_path.substring(0, 1);

    // 当父节点path的末尾不是/且子节点的开头不是/时，才添加/
    if (parent_path_last_char != '/' && child_path_first_char != '/') {
        child_path = child_path + '/' + child_relative_path;
    } else {
        child_path = child_path + child_relative_path;
    }
    return child_path;
}

// =========================== traverse_tree function utils ========================

export function traverse_tree(tree, parent_path, op) {
    for (var i = 0; i < tree.length;) {
        var child = tree[i];
        // 获取当前节点的绝对path
        var child_path = get_child_absolute_path(child.path, parent_path);

        var flag = {
            visit_children: true,
            if_continue: false,
        };
        op(i, child, child_path, tree, flag);

        if (flag.if_continue === true) {
            continue;
        }

        // 遍历当前节点的子节点
        if (flag.visit_children === true) {
            if (child.children != undefined) {
                traverse_tree(child.children, child_path, op);
            }
        }

        i++;
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

export function cloneDeep(value, parent) {
    // 判断拷贝的数据类型，如果为原始类型数据，直接返回其值
    if (isPrimitiveValue(value)) {
        return value;
    }
    // 定义一个保存引用类型的变量,根据 引用数据类型不同的子类型初始化不同的值，以下对象类型的判断和初始化可以根据自身功能的需要做删减。这里列出了所有的引用类型的场景。
    let result;

    if (typeof value === 'function') {
        // result=value 如果复制函数的时候需要保持同一个引用可以省去新函数的创建，这里用eval创建了一个原函数的副本
        // result = eval(`(${value.toString()})`);
        // 不克隆函数
        return;
    } else if (Array.isArray(value)) {
        result = [];
    } else if (value instanceof RegExp) {
        result = new RegExp(value);
    } else if (value instanceof Date) {
        result = new Date(value);
    } else if (value instanceof Number) {
        result = new Number(value);
    } else if (value instanceof String) {
        result = new String(value);
    } else if (value instanceof Boolean) {
        result = new Boolean(value);
    } else if (typeof value === 'object') {
        result = new Object();
    }

    for (const key in value) {
        if (value.hasOwnProperty(key)) {
            try {
                result[key] = cloneDeep(value[key], value); // 属性值为原始类型包装对象的时候，（Number,String,Boolean）这里会抛错，需要加一个错误处理，对运行结果没有影响。
            } catch (error) {
                console.log(error, value, value[key]);
            }
        }
    }

    return result;
}
