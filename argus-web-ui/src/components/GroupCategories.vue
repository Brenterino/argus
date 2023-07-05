<template>
	<div>
		<table class="custom-table">
			<tr class="custom-table-header">
				<th>Category</th>
				<th>Symbol</th>
				<th>Color</th>
				<th />
			</tr>
			<tr v-for="category in metadata.categories" :key="category.name">
				<td class="custom-table-row">{{ category.name }}</td>
				<td class="custom-table-row">
					<input class="custom-table-text-input" type="text" v-model="category.symbol" />
				</td>
				<td class="custom-table-row">
					<input type="text" v-model="category.color"/>
				</td>
				<td class="custom-table-row">
					<button class="custom-table-button" @click="deleteCategory(category.name)">
						<img src="../assets/leave.svg" class="custom-table-button-svg" />
					</button>
				</td>
			</tr>
			<tr>
				<td class="custom-table-row">
					<input class="custom-table-text-input" type="text" v-model="newCategory.name" />
				</td>
				<td class="custom-table-row">
					<input class="custom-table-text-input" type="text" v-model="newCategory.symbol" />
				</td>
				<td class="custom-table-row">
					<input type="text" v-model="newCategory.color"/>
				</td>
				<td class="custom-table-row">
					<button class="custom-table-button" @click="addCategory()">
						<img src="../assets/add.svg" class="custom-table-button-svg" />
					</button>
				</td>
			</tr>
		</table>
	</div>
</template>

<script>
export default {
	name: "group-config",
	data: () => ({
		group: null,
		newCategory: {
			name: "",
			symbol: "",
			color: ""
		},
		metadata: []
	}),
	mounted() {
		this.group = this.$route.params.group;
		this.metadata = {
			categories: [
				{
					name: "Ally",
					symbol: "\u2661",
					color: "#34EB37",
				}
			]
		}
	},
	methods: {
		deleteCategory(category) {
			console.log("delete category " + category)
		},
		addCategory() {
			console.log("add category " + JSON.stringify(this.newCategory))
		}
	}
}
</script>
