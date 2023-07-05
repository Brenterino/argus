import { defineStore } from 'pinia'
import { useLocalStore } from './local'
import { isRead, isWrite, toRawPermission } from '../util/permission'
import axios from 'axios'

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
                group.canToggle = true;
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
        async updateElection(groupName, toggleRead, toggleWrite) {
            const targetGroup = this.groups.find(group => group.name === groupName)
            targetGroup.canToggle = false
            const readAccess = toggleRead ? !targetGroup.readAccess : targetGroup.readAccess;
            const writeAccess = toggleWrite ? !targetGroup.writeAccess : targetGroup.writeAccess;
            const targetPermission = toRawPermission(readAccess, writeAccess)
            if (targetGroup != null) {
                const local = useLocalStore()
                await local.fetchHost()
                await local.fetchToken()
                try {
                    const config = local.getTokenHeader
                    await axios.put(local.host + '/groups/permissions/' + groupName,
                        {
                            uuid: "00000000-0000-0000-0000-000000000000",
                            permission: targetPermission
                        },
                        config)
                } catch (error) {
                    alert(error?.response?.data)
                    await this.fetchGroups() // force refresh :)
                }
            }
            targetGroup.canToggle = true
        },
        async createGroup(groupName) {
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = local.getTokenHeader
                await axios.post(local.host + '/groups/' + groupName + "/admin", {}, config)
                await this.fetchGroups()
            } catch (error) {
                alert(error?.response?.data)
            }
        },
        async leaveGroup(groupName) {
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = local.getTokenHeader
                await axios.delete(local.host + '/groups/' + groupName, config)
                await this.fetchGroups()
            } catch (error) {
                alert(error?.response?.data)
            }
        },
        async fetchGroups() {
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = local.getTokenHeader
                const groupsApi = await axios.get(local.host + '/groups', config)
                const electApi = await axios.get(local.host + '/groups/permissions', config)
                this.groups = extractGroups(groupsApi.data)
                addElections(electApi.data, this.groups)
            } catch (error) {
                alert("Could not fetch groups!")
            }
        }
    }
})
