import Vue from 'vue';

import 'normalize.css/normalize.css'; // A modern alternative to CSS resets
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import locale from 'element-ui/lib/locale/lang/en'; // lang i18n
import '@/styles/index.scss'; // global css
import App from './App';
import store from './store';
import router from './router';

import '@/icons'; // icon
import '@/permission'; // permission control
import VueMarkdownEditor from '@kangc/v-md-editor';
import '@kangc/v-md-editor/lib/style/base-editor.css';
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js';
import '@kangc/v-md-editor/lib/theme/style/vuepress.css';

// Prism
import Prism from 'prismjs';
// highlight code
import 'prismjs/components/prism-json';
import VMdPreview from '@kangc/v-md-editor/lib/preview';
import '@kangc/v-md-editor/lib/style/preview.css';
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js';
import '@kangc/v-md-editor/lib/theme/style/github.css';

// highlightjs
import hljs from 'highlight.js';
import createCopyCodePlugin from '@kangc/v-md-editor/lib/plugins/copy-code/index';
import '@kangc/v-md-editor/lib/plugins/copy-code/copy-code.css';

import createCopyCodePreview from '@kangc/v-md-editor/lib/plugins/copy-code/preview';
import '@/assets/icon/iconfont.css';

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
if (process.env.NODE_ENV === 'production') {
    const {mockXHR} = require('../mock');
    mockXHR();
}

// set ElementUI lang to EN
Vue.use(ElementUI, {locale});
// 如果想要中文版 element-ui，按如下方式声明
// Vue.use(ElementUI)

Vue.config.productionTip = false;

VueMarkdownEditor.use(vuepressTheme, {
    Prism,
});

Vue.use(VueMarkdownEditor);

VMdPreview.use(githubTheme, {
    Hljs: hljs,
});

Vue.use(VMdPreview);

VueMarkdownEditor.use(createCopyCodePlugin());
VMdPreview.use(createCopyCodePreview());

new Vue({
    el: '#app',

    router,
    store,
    render: (h) => h(App),
});
