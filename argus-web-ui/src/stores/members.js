import { defineStore } from 'pinia'
import { useLocalStore } from './local'
import { isRead, isWrite, toRawPermission } from '../util/permission'
import axios from 'axios'

const extractMembers = (data) => {
    let members = [];
    data?.permissions?.forEach(member => {
        const uuid = member.uuid
        const readable = isRead(member.permission);
        const writable = isWrite(member.permission);
        const adminRole = member.permission === "ADMIN";
        members.push({
            uuid: uuid,
            canRead: readable,
            canWrite: writable,
            isAdmin: adminRole,
            canToggle: true
        })
    })
    return members;
}

export const useMembersStore = defineStore("membersStore", {
    state: () => ({
        members: [],
        pageCount: 1
    }),
    getters: {
        getMembers(state) {
            return state.members
        },
        getPages(state) {
            return state.pageCount
        }
    },
    actions: {
        async inviteMember(group, page, newUser) {
            const targetPermission = toRawPermission(newUser.canRead, newUser.canWrite, newUser.isAdmin)
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = local.getTokenHeader
                await axios.post(local.host + '/groups/permissions/' +
                    group + '/admin',
                    {
                        uuid: newUser.uuid,
                        permission: targetPermission
                    },
                    config)
            } catch (error) {
                alert(error?.response?.data)
            }
            await this.fetchMembers(group, page) // force refresh :)
        },
        async kickMember(group, page, uuid) {
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = local.getTokenHeader
                config.data = { // attach body to delete config
                    uuid: uuid,
                    name: ""
                };
                await axios.delete(local.host + '/groups/permissions/' +
                    group + '/admin', config)
            } catch (error) {
                alert(error?.response?.data)
            }
            await this.fetchMembers(group, page) // force refresh :)
        },
        async updatePermission(group, page, uuid, toggleRead, toggleWrite, toggleAdmin) {
            const targetMember = this.members.find(member => member.uuid === uuid)
            targetMember.canToggle = false
            const readAccess = toggleRead ? !targetMember.canRead : targetMember.canRead;
            const writeAccess = toggleWrite ? !targetMember.canWrite : targetMember.canWrite;
            const adminAccess = toggleAdmin ? !targetMember.isAdmin : targetMember.isAdmin;
            const targetPermission = toRawPermission(readAccess, writeAccess, adminAccess)
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = local.getTokenHeader
                await axios.put(local.host + '/groups/permissions/' +
                    group + '/admin',
                    {
                        uuid: uuid,
                        permission: targetPermission
                    },
                    config)
            } catch (error) {
                alert(error?.response?.data)
                await this.fetchMembers(group, page) // force refresh :)
            }
            targetMember.canToggle = true
        },
        async fetchMembers(group, page) {
            const local = useLocalStore()
            await local.fetchHost()
            await local.fetchToken()
            try {
                const config = local.getTokenHeader
                const membersApi = await axios.get(local.host + '/groups/permissions/' +
                    group + '/admin?page=' + page, config)
                this.members = extractMembers(membersApi.data)
                this.pageCount = membersApi.data.pages
            } catch (error) {
                alert(error?.response?.data)
            }
        }
    }
})
