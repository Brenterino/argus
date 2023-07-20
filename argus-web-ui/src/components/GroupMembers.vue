<template>
	<div>
		<table class="custom-table">
			<tr class="custom-table-header">
				<th>UUID</th>
				<th>Name</th>
				<th>Read</th>
				<th>Write</th>
				<th>Admin</th>
				<th />
			</tr>
			<tr v-for="member in membersStore.getMembers" :key="member.uuid">
				<td class="custom-table-row">{{ member.uuid }}</td>
				<td class="custom-table-row">{{ getNameByUUID(member.uuid) }}</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="member.canRead" :disabled="!member.canToggle || member.isAdmin"
						@click="toggle(member.uuid, true, false, false)" />
				</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="member.canWrite" :disabled="!member.canToggle || member.isAdmin"
						@click="toggle(member.uuid, false, true, false)" />
				</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="member.isAdmin" :disabled="!member.canToggle"
						@click="toggle(member.uuid, false, false, true)" />
				</td>
				<td class="custom-table-row">
					<button class="custom-table-button" @click="kickMember(member.uuid)">
						<img src="../assets/leave.svg" class="custom-table-button-svg" />
					</button>
				</td>
			</tr>
			<tr>
				<td class="custom-table-row">
					<input class="custom-table-text-input" type="text" v-model="newUser.uuid" />
				</td>
				<td class="custom-table-row"></td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="newUser.canRead" :disabled="newUser.isAdmin" />
				</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="newUser.canWrite" :disabled="newUser.isAdmin" />
				</td>
				<td class="custom-table-row">
					<input type="checkbox" v-model="newUser.isAdmin" />
				</td>
				<td class="custom-table-row">
					<button class="custom-table-button" @click="addMember()">
						<img src="../assets/add.svg" class="custom-table-button-svg" />
					</button>
				</td>
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
					{{ currentPage }} / {{ membersStore.getPages }}
				</td>
				<td class="navigation-table-column-right">
					<button class="navigation-table-button" :disabled="currentPage == membersStore.getPages"
						@click="pageRight()">&gt;</button>
				</td>
				<td class="navigation-table-column-far-right">
					<button class="navigation-table-button" :disabled="currentPage == membersStore.getPages"
						@click="pageLast()">&Gt;</button>
				</td>
			</tr>
		</table>
	</div>
</template>

<script>
import { computed } from 'vue';
import { useGroupsStore } from '../stores/groups.js';
import { useMembersStore } from '../stores/members.js';

export default {
	name: "group-members",
	data: () => ({
		group: null,
		currentPage: null,
		newUser: {
			uuid: "",
			canRead: false,
			canWrite: false,
			isAdmin: false
		},
		metadata: []
	}),
	setup() {
		const groupsStore = useGroupsStore();
		const membersStore = useMembersStore();
		return { groupsStore, membersStore };
	},
	mounted() {
		this.currentPage = 1;
		this.group = this.$route.params.group;
		this.groupsStore.fetchGroups();
		this.membersStore.fetchMembers(this.group, this.currentPage - 1);
		this.metadata = computed(() => {
			return this.groupsStore.getGroups
				.find(g => g.name === this.group)?.metadata;
		});
	},
	methods: {
		pageFirst() {
			this.currentPage = 1;
			this.membersStore.fetchMembers(this.group, this.currentPage - 1);
		},
		pageLeft() {
			this.currentPage--;
			this.membersStore.fetchMembers(this.group, this.currentPage - 1);
		},
		pageRight() {
			this.currentPage++;
			this.membersStore.fetchMembers(this.group, this.currentPage - 1);
		},
		pageLast() {
			this.currentPage = this.membersStore.getPages;
			this.membersStore.fetchMembers(this.group, this.currentPage - 1);
		},
		toggle(uuid, toggleRead, toggleWrite, toggleAdmin) {
			this.membersStore.updatePermission(this.group, this.currentPage - 1,
				uuid, toggleRead, toggleWrite, toggleAdmin);
		},
		kickMember(uuid) {
			this.membersStore.kickMember(this.group, this.currentPage - 1, uuid);
		},
		addMember() {
			this.membersStore.inviteMember(this.group, this.currentPage - 1, this.newUser);
			this.newUser = {
				uuid: "",
				canRead: false,
				canWrite: false,
				isAdmin: false
			};
		},
		getNameByUUID(uuid) {
			const alignment = this.metadata?.alignments?.find(a => a.uuid === uuid);
			return alignment?.name || 'N/A';
		}
	}
}
</script>
