import { defineStore } from 'pinia'
import axios from 'axios'

export const useLocalStore = defineStore("localStore", {
    state: () => ({
        tokenHolder: null,
        host: null
    }),
    getters: {
        getToken(state) {
            return state.tokenHolder.token
        },
        isTokenExpired(state) {
            if (state.tokenHolder === null)
                return true;
            const expiration = Date.parse(state.tokenHolder.expiration)
                .getTime()
            const now = Date.now()
                .getTime()
            return expiration > now
        }
    },
    actions: {
        async fetchToken() {
            try {
                const resp = await axios.get('http://localhost:9000/api/token')
                this.tokenHolder = resp.data
            } catch (error) {
                alert("Could not fetch token from running instance.")
                console.log(error)
            }
        },
        async fetchHost() {
            try {
                const resp = await axios.get('http://localhost:9000/api/host')
                this.host = resp.data
            } catch (error) {
                alert("Could not fetch host from running instance.")
                console.log(error)
            }
        }
    }
})
