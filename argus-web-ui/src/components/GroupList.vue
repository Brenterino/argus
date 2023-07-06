<template>
	<table class="custom-table">
		<tr class="custom-table-header">
			<th>Group</th>
			<th>Read</th>
			<th>Write</th>
			<th>Modify</th>
			<th>Leave</th>
		</tr>
		<tr v-for="group in groupsStore.getGroups" :key="group.name">
			<td class="custom-table-row">{{ group.name }}</td>
			<td class="custom-table-row">
				<input type="checkbox" v-model="group.readActive" :disabled="!group.readAvailable || !group.canToggle"
					@click="toggle(group.name, !group.readActive, group.writeActive)" />
			</td>
			<td class="custom-table-row">
				<input type="checkbox" v-model="group.writeActive" :disabled="!group.writeAvailable || !group.canToggle"
					@click="toggle(group.name, group.readActive, !group.writeActive)" />
			</td>
			<td class="custom-table-row">
				<router-link :to="{ name: 'group-view', params: { group: group.name } }" custom v-slot="{ navigate }">
					<button :disabled="!group.admin" class="custom-table-button"
						:class="{ 'custom-table-button-disabled': !group.admin }" @click="navigate"
						@keypress.enter="navigate" role="link">
						<img src="../assets/edit.svg" class="custom-table-button-svg" />
					</button>
				</router-link>
			</td>
			<td class="custom-table-table-row">
				<button class="custom-table-button" @click="leaveGroup(group.name)">
					<img src="../assets/leave.svg" class="custom-table-button-svg" />
				</button>
			</td>
		</tr>
		<tr>
			<td class="custom-table-row">
				<input class="custom-table-text-input" type="text" v-model="newGroup.name" />
			</td>
			<td />
			<td />
			<td />
			<td class="custom-table-row">
				<button class="custom-table-button" @click="createGroup()">
					<img src="../assets/add.svg" class="custom-table-button-svg" />
				</button>
			</td>
		</tr>
	</table>
</template>

<script>
import { useGroupsStore } from '../stores/groups.js';

export default {
	name: "group-list",
	data: () => ({
		newGroup: {
			name: ""
		}
	}),
	setup() {
		const groupsStore = useGroupsStore();
		return { groupsStore };
	},
	mounted() {
		this.groupsStore.fetchGroups();
	},
	methods: {
		toggle(groupName, readActive, writeActive) {
			this.groupsStore.updateElection(groupName, readActive, writeActive);
		},
		leaveGroup(groupName) {
			if (confirm("Are you sure you want to leave?")) {
				this.groupsStore.leaveGroup(groupName);
			}
		},
		createGroup() {
			this.groupsStore.createGroup(this.newGroup.name);
			this.newGroup.name = "";
		}
	}
}
</script>
