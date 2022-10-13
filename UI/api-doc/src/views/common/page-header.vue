<template>
    <!-- 页面头 -->
    <div class="mgb20 bc-b-gray bc-t-gray bg-gray" style="height: 60px">
        <div
            class="pdt10 pdb10"
            style="width: 95%; height: 100%; margin: 0 auto 0 auto"
        >
            <span class="fs18" style="line-height: 2.2">{{ title }}</span>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        title: '',
    },
};
</script>
