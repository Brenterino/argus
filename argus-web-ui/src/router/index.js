import { createRouter, createWebHistory } from "vue-router"
import GroupView from "../views/GroupView"
import GroupsView from "../views/GroupsView"

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            name: "groups-view",
            path: "/",
            component: GroupsView
        },
        {
            name: "group-view",
            path: "/groups/:group",
            component: GroupView
        }
    ]
})

export default router
