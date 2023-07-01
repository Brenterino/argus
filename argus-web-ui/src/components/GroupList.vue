<template>
	<table class="custom-table">
		<tr class="custom-table-header">
			<th>Group</th>
			<th>Read</th>
			<th>Write</th>
			<th>Modify</th>
			<th>Leave</th>
		</tr>
		<tr v-for="group in groups" :key="group.name">
			<td class="custom-table-row">{{ group.name }}</td>
			<td class="custom-table-row">
				<input type="checkbox" v-model="group.readActive" :disabled="!group.readAvailable"
					@click="toggleRead(group.name)"/>
			</td>
			<td class="custom-table-row">
				<input type="checkbox" v-model="group.writeActive" :disabled="!group.writeAvailable"
					@click="toggleWrite(group.name)"/>
			</td>
			<td class="custom-table-row">
				<router-link :to="{ name: 'group-view', params: { group: group.name } }"
					custom v-slot="{ navigate }">
					<button :disabled="!group.admin" class="custom-table-button"
						:class="{ 'custom-table-button-disabled': !group.admin }"
						@click="navigate" @keypress.enter="navigate" role="link">
						<img src="../assets/edit.svg" class="custom-table-button-svg" />
					</button>
				</router-link>
			</td>
			<td class="custom-table-table-row">
				<button class="custom-table-button"
					@click="leaveGroup(group.name)">
					<img src="../assets/leave.svg" class="custom-table-button-svg" />
				</button>
			</td>
		</tr>
	</table>
</template>

<script>
export default {
	name: "group-list",
	data: () => ({
		groups: null
	}),
	mounted() {
		this.groups = [{
			name: "Volterra",
			readActive: true,
			readAvailable: true,
			writeActive: true,
			writeAvailable: true,
			admin: true
		},
		{
			name: "Estalia",
			readActive: true,
			readAvailable: true,
			writeActive: false,
			writeAvailable: false,
			admin: false
		},
		{
			name: "Butternut",
			readActive: false,
			readAvailable: true,
			writeActive: false,
			writeAvailable: true,
			admin: false
		},
		{
			name: "Icenia",
			readActive: false,
			readAvailable: false,
			writeActive: true,
			writeAvailable: true,
			admin: false
		}];
	},
	methods: {
		toggleRead(group) {
			console.log("toggling read for group " + group)
		},
		toggleWrite(group) {
			console.log("toggling write for group " + group)
		},
		leaveGroup(group) {
			console.log("leaving group " + group)
		}
	}
}
</script>
