import { defineStore } from 'pinia'
import axios from 'axios'

export const useLocalStore = defineStore("localStore", {
    state: () => ({
        tokenHolder: null,
        host: null
    }),
    getters: {
        getTokenHeader(state) {
            return {
                headers: {
                    'Authorization': 'Bearer ' + state.getToken
                }
            };
        },
        getToken(state) {
            return state.tokenHolder.token;
        }
    },
    actions: {
        async fetchToken() {
            try {
                const resp = await axios.get('http://localhost:9000/api/token');
                this.tokenHolder = resp.data;
            } catch (error) {
                alert("Could not fetch token from running instance.");
            }
        },
        async fetchHost() {
            try {
                const resp = await axios.get('http://localhost:9000/api/host');
                this.host = resp.data;
            } catch (error) {
                alert("Could not fetch host from running instance.");
            }
        }
    }
})
