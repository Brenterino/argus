import { createRouter, createWebHistory } from "vue-router";
import GroupsView from "../views/GroupsView";
import GroupView from "../views/GroupView";

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

export default router;
