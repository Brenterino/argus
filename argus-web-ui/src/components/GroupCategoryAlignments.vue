<template>
    <div>
        <table class="custom-table">
            <tr class="custom-table-header">
                <th>UUID</th>
                <th>Name</th>
                <th>Alignment</th>
                <th />
                <th />
            </tr>
            <tr v-for="alignment in metadata?.alignments" :key="alignment.uuid">
                <td class="custom-table-row">{{ alignment.uuid }}</td>
                <td class="custom-table-row">
                    <input class="custom-table-text-input" type="text" v-model="alignment.name" />
                </td>
                <td class="custom-table-row">
                    <select v-model="alignment.alignment">
                        <option v-for="category in metadata?.categories" :key="category.name">
                            {{ category.name }}
                        </option>
                    </select>
                </td>
                <td class="custom-table-row">
                    <button class="custom-table-button" @click="updatealignment(alignment.uuid)">
                        <img src="../assets/update.svg" class="custom-table-button-svg" />
                    </button>
                </td>
                <td class="custom-table-row">
                    <button class="custom-table-button" @click="deletealignment(alignment.uuid)">
                        <img src="../assets/leave.svg" class="custom-table-button-svg" />
                    </button>
                </td>
            </tr>
            <tr>
                <td class="custom-table-row">
                    <input class="custom-table-text-input" type="text" v-model="newalignment.uuid" />
                </td>
                <td class="custom-table-row">
                    <input class="custom-table-text-input" type="text" v-model="newalignment.name" />
                </td>
                <td class="custom-table-row">
                    <select v-model="newalignment.alignment">
                        <option disabled value="">Select one...</option>
                        <option v-for="category in metadata?.categories" :key="category.name">
                            {{ category.name }}
                        </option>
                    </select>
                </td>
                <td class="custom-table-row">
                    <button class="custom-table-button" @click="addalignment()">
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
        newalignment: {
            uuid: "",
            name: "",
            alignment: ""
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
        deletealignment(uuid) {
            this.metadata.alignments =
                this.metadata.alignments.filter(a => a.uuid != uuid);
            this.groupsStore.updateMetadata(this.group);
        },
        updatealignment(uuid) {
            const targetalignment = this.metadata.alignments.find(a => a.uuid === uuid);
            if (targetalignment.alignment) {
                this.groupsStore.updateMetadata(this.group);
            } else {
                alert("Check alignment input as it is invalid.");
            }
        },
        addalignment() {
            if (this.metadata.alignments == null) {
                this.metadata.alignments = [];
            }
            const targetalignment = this.metadata.alignments.find(a => a.uuid === this.newalignment.uuid);
            if (targetalignment != null) {
                alert("This user is already entered!");
                return;
            }
            console.log(this.newalignment)
            if (this.newalignment.uuid && this.newalignment.alignment && isUUID(this.newalignment.uuid)) {
                this.metadata.alignments.push(this.newalignment);
                this.newalignment = {
                    uuid: "",
                    name: "",
                    alignment: ""
                };
                this.groupsStore.updateMetadata(this.group);
            } else {
                alert("Check alignment input as it is invalid.");
            }
        }
    }
}
</script>
