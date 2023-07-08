<template>
    <div>Hello, {{ group }}</div>
</template>

<script>
export default {
    name: "group-extended",
    data: () => ({
        group: null
    }),
    mounted() {
        this.group = this.$route.params.group;
    }
}
</script>
