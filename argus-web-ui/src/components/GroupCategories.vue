<template>
	<div>
		<table class="custom-table">
			<tr class="custom-table-header">
				<th>Category</th>
				<th>Symbol</th>
				<th>Color</th>
				<th />
				<th />
			</tr>
			<tr v-for="category in metadata?.categories" :key="category.name">
				<td class="custom-table-row">{{ category.name }}</td>
				<td class="custom-table-row">
					<input class="custom-table-text-input" type="text" v-model="category.symbol" />
				</td>
				<td class="custom-table-row">
					<input type="text" v-model="category.color" />
				</td>
				<td class="custom-table-row">
					<button class="custom-table-button" @click="updateCategory(category.name)">
						<img src="../assets/update.svg" class="custom-table-button-svg" />
					</button>
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
					<input type="text" v-model="newCategory.color" />
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
import { computed } from 'vue';
import { useGroupsStore } from '../stores/groups.js';

const isColor = (color) => {
	const s = new Option().style;
	s.color = color;
	return s.color !== '';
};

export default {
	name: "group-categories",
	data: () => ({
		group: null,
		newCategory: {
			name: "",
			symbol: "",
			color: ""
		},
		metadata: []
	}),
	setup() {
		const groupsStore = useGroupsStore();
		return { groupsStore };
	},
	mounted() {
		this.group = this.$route.params.group;
		this.groupsStore.fetchGroups();
		this.metadata = computed(() => {
			return this.groupsStore.getGroups
				.find(g => g.name === this.group)?.metadata;
		});
	},
	methods: {
		deleteCategory(category) {
			if (confirm("Are you sure you want to delete this category? This will delete all alignments using this category.")) {
				this.metadata.categories =
					this.metadata.categories.filter(c => c.name != category);
				this.metadata.alignments =
					this.metadata.alignments?.filter(a => a.assignment != category);
				this.groupsStore.updateMetadata(this.group);
			}
		},
		updateCategory(category) {
			const targetCategory = this.metadata.categories.find(c => c.name === category);
			if (targetCategory.symbol && isColor(targetCategory.color)) {
				this.groupsStore.updateMetadata(this.group);
			} else {
				alert("Check category input as it is invalid.");
			}
		},
		addCategory() {
			if (this.metadata.categories == null) {
				this.metadata.categories = [];
			}
			const targetCategory = this.metadata.categories.find(c => c.name === this.newCategory.name);
			if (targetCategory != null) {
				alert("Category with this name already exists!");
				return;
			}
			if (this.newCategory.name && this.newCategory.symbol && isColor(this.newCategory.color)) {
				this.metadata.categories.push(this.newCategory);
				this.newCategory = {
					name: "",
					symbol: "",
					color: ""
				};
				this.groupsStore.updateMetadata(this.group);
			} else {
				alert("Check category input as it is invalid.");
			}
		}
	}
}
</script>
