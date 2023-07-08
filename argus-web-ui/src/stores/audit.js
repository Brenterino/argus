import { defineStore } from 'pinia'
import { useLocalStore } from './local.js'
import axios from 'axios'

const extractAudit = (data) => {
    console.log(JSON.stringify(data))
    let audit = [];
    data?.log?.forEach(log => {
        audit.push({
            changer: log.changer,
            target: log.target,
            action: log.action,
            permission: log.permission,
            occurred: log.occurred
        });
    });
    return audit;
}

export const useAuditStore = defineStore("auditStore", {
    state: () => ({
        audit: [],
        pageCount: 1
    }),
    getters: {
        getAudit(state) {
            return state.audit;
        },
        getPages(state) {
            return state.pageCount;
        }
    },
    actions: {
        async fetchAudit(group, page) {
            const local = useLocalStore();
            await local.fetchHost();
            await local.fetchToken();
            try {
                const config = local.getTokenHeader;
                const auditApi = await axios.get(local.host + '/groups/audits/' +
                    group + '?page=' + page, config);
                this.audit = extractAudit(auditApi.data);
                this.pageCount = auditApi.data.pages;
            } catch (error) {
                alert(error?.response?.data);
            }
        }
    }
})
