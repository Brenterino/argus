<template>
    <div>Hello, {{ group }}</div>
</template>
    
<script>
export default {
    name: "group-audit",
    data: () => ({
        group: null
    }),
    mounted() {
        this.group = this.$route.params.group;
    }
}
</script>
