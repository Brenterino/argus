<template>
	<div>
		<table class="custom-table">
			<tr class="custom-table-header">
				<th>UUID</th>
				<th>Read</th>
				<th>Write</th>
				<th>Admin</th>
				<th>Kick</th>
			</tr>
			<tr v-for="member in members" :key="member.uuid">
				<td class="custom-table-row">{{ member.uuid }}</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="member.canRead"
						@click="toggleRead(member.uuid)"/>
				</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="member.canWrite"
						@click="toggleWrite(member.uuid)"/>
				</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="member.isAdmin"
						@click="toggleAdmin(member.uuid)"/>
				</td>
				<td class="custom-table-row">
					<button class="custom-table-button"
						@click="kickMember(member.uuid)">
						<img src="../assets/leave.svg" class="custom-table-button-svg" />
					</button>
				</td>
			</tr>
		</table>
		<table class="navigation-table">
			<tr>
				<td class="navigation-table-column-far-left">
					<button class="navigation-table-button"
						:disabled="currentPage == 1"
						@click="pageFirst()">&Lt;</button>
				</td>
				<td class="navigation-table-column-left">
					<button class="navigation-table-button"
						:disabled="currentPage == 1"
						@click="pageLeft()">&lt;</button>
				</td>
				<td class="navigation-table-column-mid">
					{{ currentPage }} / {{ totalPages }}
				</td>
				<td class="navigation-table-column-right">
					<button class="navigation-table-button"
						:disabled="currentPage == totalPages"
						@click="pageRight()">&gt;</button>
				</td>
				<td class="navigation-table-column-far-right">
					<button class="navigation-table-button"
						:disabled="currentPage == totalPages"
						@click="pageLast()">&Gt;</button>
				</td>
			</tr>
		</table>
	</div>
</template>

<script>
export default {
	name: "group-members",
	data: () => ({
		currentPage: null,
		totalPages: null,
		members: null
	}),
	mounted() {
		this.currentPage = 1;
		this.totalPages = 10;
		this.members = [{
			uuid: "81646d40-0247-4768-b67b-25c22400406f",
			canRead: true,
			canWrite: true,
			isAdmin: true
		},
		{
			uuid: "851080f4-507f-4f71-995f-f992f1fe70d1",
			canRead: true,
			canWrite: true,
			isAdmin: false
		},
		{
			uuid: "4af4ccd3-0eb1-405d-aae1-bdaf9e9cf69d",
			canRead: false,
			canWrite: true,
			isAdmin: false
		},
		{
			uuid: "8c344f73-ebcd-4eda-9a4b-106abc8be1d1",
			canRead: true,
			canWrite: false,
			isAdmin: false
		},
		]
	},
	methods: {
		pageFirst() {
			this.currentPage = 1;
		},
		pageLeft() {
			this.currentPage--;
		},
		pageRight() {
			this.currentPage++;
		},
		pageLast() {
			this.currentPage = this.totalPages;
		},
		toggleRead(uuid) {
			console.log("toggle read for " + uuid)
		},
		toggleWrite(uuid) {
			console.log("toggle write for " + uuid)
		},
		toggleAdmin(uuid) {
			console.log("toggle admin for " + uuid)
		},
		kickMember(uuid) {
			console.log("kicking member " + uuid)
		}
	}
}
</script>
