import { defineStore } from 'pinia'
import { useLocalStore } from './local'
import axios from 'axios'

const isRead = (permission) => {
    return permission === "READ" ||
            permission === "READWRITE" ||
            permission === "ADMIN";
}

const isWrite = (permission) => {
    return permission === "WRITE" ||
            permission === "READWRITE" ||
            permission === "ADMIN";
}

const extractGroups = (data) => {
    let groups = [];
    data?.permissions?.forEach(permission => {
        const groupName = permission.group.name;
        const meta = permission.group.metadata;
        const readable = isRead(permission.permission);
        const writable = isWrite(permission.permission);
        const adminRole = permission.permission === "ADMIN";
        groups.push({
            name: groupName,
            readAvailable: readable,
            writeAvailable: writable,
            admin: adminRole,
            metadata: meta
        });
    })
    return groups;
}

const addElections = (data, groups) => {
    data?.permissions?.forEach(permission => {
        const groupName = permission.group.name;
        const readable = isRead(permission.permission);
        const writable = isWrite(permission.permission);
        groups.forEach(group => { // TODO fix stuff so this can be O(n) instead of O(n^2), idk bros im bad at frontend
            if (group.name === groupName) {
                group.readActive = readable;
                group.writeActive = writable;
            }
        })
    });
}

export const useGroupsStore = defineStore("groupsStore", {
    state: () => ({
        groups: []
    }),
    getters: {
        getGroups(state) {
            return state.groups
        }
    },
    actions: {
        async fetchGroups() {
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = {
                    headers: {
                        'Authorization': 'Bearer ' + local.getToken
                    }
                }
                const groupsApi = await axios.get(local.host + '/groups', config)
                const electApi = await axios.get(local.host + '/groups/permissions', config)
                this.groups = extractGroups(groupsApi.data)
                addElections(electApi.data, this.groups)
            } catch (error) {
                alert("Could not fetch groups!")
                console.log(error)
            }
        }
    }
})
