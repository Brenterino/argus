<template>
	<table class="group-table">
		<tr class="group-table-header">
			<th>Group</th>
			<th>Read</th>
			<th>Write</th>
			<th>Modify</th>
			<th>Leave</th>
		</tr>
		<tr v-for="group in groups" :key="group.name">
			<td class="group-table-row">{{ group.name }}</td>
			<td class="group-table-row">
				<input type="checkbox" v-model="group.readActive" :disabled="!group.readAvailable" />
			</td>
			<td class="group-table-row">
				<input type="checkbox" v-model="group.writeActive" :disabled="!group.writeAvailable" />
			</td>
			<td class="group-table-row">
				<button :disabled="!group.admin" class="group-button"
					:class="{ 'group-button-disabled': !group.admin }">
					<img src="../assets/edit.svg" class="group-button-svg" />
				</button>
			</td>
			<td class="group-table-row">
				<button class="group-button">
					<img src="../assets/leave.svg" class="group-button-svg" />
				</button>
			</td>
		</tr>
	</table>
</template>

<script>
export default {
	name: "group-list",
	data: () => ({
		groups: []
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
	}
}
</script>

<style>
.group-table {
	border-collapse: collapse;
	font-size: 1rem;
	font-family: sans-serif;
	min-width: 400px;
	box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);

	.group-table-header {
		box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);
	}

	.group-table-row {
		padding-top: 10px;
	}

	.group-button {
		cursor: pointer;
		background-color: transparent;
		border: none;
	}

	.group-button-disabled {
		cursor: not-allowed;
	}

	.group-button-svg {
		width: 30px;
	}
}</style>
