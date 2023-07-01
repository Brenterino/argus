<template>
	<div>Hello, {{ group }}</div>
</template>

<script>
export default {
	name: "group-config",
	data: () => ({
		group: null,
		metadata: []
	}),
	mounted() {
		this.group = this.$route.params.group;
	}
}
</script>
