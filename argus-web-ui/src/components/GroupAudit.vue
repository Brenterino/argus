<template>
	<div>
		<table class="custom-table custom-table">
			<tr class="custom-table-header">
				<th>Changer UUID</th>
				<th>Target UUID</th>
				<th>Action</th>
				<th>Permission</th>
				<th>Time</th>
				<th />
			</tr>
			<tr v-for="audit in auditStore.getAudit" :key="audit.occurred">
				<td class="custom-table-row">{{ audit.changer }}</td>
				<td class="custom-table-row">{{ audit.target }}</td>
				<td class="custom-table-row">{{ audit.action }}</td>
				<td class="custom-table-row">{{ audit.permission }}</td>
				<td class="custom-table-row">{{ audit.occurred }}</td>
			</tr>
		</table>
		<table class="navigation-table">
			<tr>
				<td class="navigation-table-column-far-left">
					<button class="navigation-table-button" :disabled="currentPage == 1" @click="pageFirst()">&Lt;</button>
				</td>
				<td class="navigation-table-column-left">
					<button class="navigation-table-button" :disabled="currentPage == 1" @click="pageLeft()">&lt;</button>
				</td>
				<td class="navigation-table-column-mid">
					{{ currentPage }} / {{ auditStore.getPages }}
				</td>
				<td class="navigation-table-column-right">
					<button class="navigation-table-button" :disabled="currentPage == auditStore.getPages"
						@click="pageRight()">&gt;</button>
				</td>
				<td class="navigation-table-column-far-right">
					<button class="navigation-table-button" :disabled="currentPage == auditStore.getPages"
						@click="pageLast()">&Gt;</button>
				</td>
			</tr>
		</table>
	</div>
</template>

<script>
import { useAuditStore } from '../stores/audit.js';

export default {
	name: "group-audit",
	data: () => ({
		group: null,
		currentPage: null
	}),
	setup() {
		const auditStore = useAuditStore();
		return { auditStore };
	},
	mounted() {
		this.currentPage = 1;
		this.group = this.$route.params.group;
		this.auditStore.fetchAudit(this.group, this.currentPage - 1);
	},
	methods: {
		pageFirst() {
			this.currentPage = 1;
			this.auditStore.fetchAudit(this.group, this.currentPage - 1);
		},
		pageLeft() {
			this.currentPage--;
			this.auditStore.fetchAudit(this.group, this.currentPage - 1);
		},
		pageRight() {
			this.currentPage++;
			this.auditStore.fetchAudit(this.group, this.currentPage - 1);
		},
		pageLast() {
			this.currentPage = this.auditStore.getPages;
			this.auditStore.fetchAudit(this.group, this.currentPage - 1);
		}
	}
}
</script>
