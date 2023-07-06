<template>
    <div>
        <table class="custom-table">
            <tr class="custom-table-header">
                <th>UUID</th>
                <th>Name</th>
                <th>Assignment</th>
                <th />
                <th />
            </tr>
            <tr v-for="assignment in metadata?.assignments" :key="assignment.uuid">
                <td class="custom-table-row">{{ assignment.uuid }}</td>
                <td class="custom-table-row">
                    <input class="custom-table-text-input" type="text" v-model="assignment.name" />
                </td>
                <td class="custom-table-row">
                    <select v-model="assignment.assignment">
                        <option v-for="category in metadata?.categories" :key="category.name">
                            {{ category.name }}
                        </option>
                    </select>
                </td>
                <td class="custom-table-row">
                    <button class="custom-table-button" @click="updateAssignment(assignment.uuid)">
                        <img src="../assets/update.svg" class="custom-table-button-svg" />
                    </button>
                </td>
                <td class="custom-table-row">
                    <button class="custom-table-button" @click="deleteAssignment(assignment.uuid)">
                        <img src="../assets/leave.svg" class="custom-table-button-svg" />
                    </button>
                </td>
            </tr>
            <tr>
                <td class="custom-table-row">
                    <input class="custom-table-text-input" type="text" v-model="newAssignment.uuid" />
                </td>
                <td class="custom-table-row">
                    <input class="custom-table-text-input" type="text" v-model="newAssignment.name" />
                </td>
                <td class="custom-table-row">
                    <select v-model="newAssignment.assignment">
                        <option disabled value="">Select one...</option>
                        <option v-for="category in metadata?.categories" :key="category.name">
                            {{ category.name }}
                        </option>
                    </select>
                </td>
                <td class="custom-table-row">
                    <button class="custom-table-button" @click="addAssignment()">
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

const isUUID = (target) => {
    const regex = /^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}$/gi;
    return regex.test(target);
};

export default {
    name: "group-categories",
    data: () => ({
        group: null,
        newAssignment: {
            uuid: "",
            name: "",
            assignment: ""
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
        deleteAssignment(uuid) {
            this.metadata.assignments =
                this.metadata.assignments.filter(a => a.uuid != uuid);
            this.groupsStore.updateMetadata(this.group);
        },
        updateAssignment(uuid) {
            const targetAssignment = this.metadata.assignments.find(a => a.uuid === uuid);
            if (targetAssignment.assignment) {
                this.groupsStore.updateMetadata(this.group);
            } else {
                alert("Check assignment input as it is invalid.");
            }
        },
        addAssignment() {
            if (this.metadata.assignments == null) {
                this.metadata.assignments = [];
            }
            const targetAssignment = this.metadata.assignments.find(a => a.uuid === this.newAssignment.uuid);
            if (targetAssignment != null) {
                alert("This user is already entered!");
                return;
            }
            console.log(this.newAssignment)
            if (this.newAssignment.uuid && this.newAssignment.assignment && isUUID(this.newAssignment.uuid)) {
                this.metadata.assignments.push(this.newAssignment);
                this.newAssignment = {
                    uuid: "",
                    name: "",
                    assignment: ""
                };
                this.groupsStore.updateMetadata(this.group);
            } else {
                alert("Check assignment input as it is invalid.");
            }
        }
    }
}
</script>
