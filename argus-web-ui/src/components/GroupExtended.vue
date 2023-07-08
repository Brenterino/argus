<template>
    <div>
        <button @click="deleteGroup()">
            Delete Group
        </button>
    </div>
</template>

<script>
import router from '@/router';
import { useGroupsStore } from '../stores/groups.js';

export default {
    name: "group-extended",
    data: () => ({
        group: null
    }),
    setup() {
        const groupsStore = useGroupsStore();
        return { groupsStore };
    },
    mounted() {
        this.group = this.$route.params.group;
        this.groupsStore.fetchGroups();
    },
    methods: {
        deleteGroup() {
            if (confirm("Are you sure you want to delete this group?")) {
                this.groupsStore.deleteGroup(this.group);
                router.push({ name: "groups-view" });
            }
        }
    }
}
</script>
