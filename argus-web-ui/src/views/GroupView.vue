<template>
  <div>
    <app-tabs :tab-list="tabList">
      <template v-slot:tabPanel-1><GroupMembers /></template>
      <template v-slot:tabPanel-2><GroupCategories /></template>
      <template v-slot:tabPanel-3><GroupAudit /></template>
    </app-tabs>
  </div>
</template>

<script>
import AppTabs from '@/components/AppTabs.vue';
import GroupMembers from '../components/GroupMembers.vue';
import GroupCategories from '../components/GroupCategories.vue';
import GroupAudit from '../components/GroupAudit.vue';

export default {
  name: 'group-config-view',
  components: {
    AppTabs,
    GroupMembers,
    GroupCategories,
    GroupAudit
  },
  data: () => ({
      tabList: [ "Members", "Categories", "Audit" ]
  })
}
</script>
