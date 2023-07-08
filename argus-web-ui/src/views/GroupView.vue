<template>
  <div>
    <app-tabs :tab-list="tabList">
      <template v-slot:tabPanel-1><GroupMembers /></template>
      <template v-slot:tabPanel-2><GroupCategories /></template>
      <template v-slot:tabPanel-3><GroupCategoryAlignments /></template>
      <template v-slot:tabPanel-4><GroupAudit /></template>
      <template v-slot:tabPanel-5><GroupExtended /></template>
    </app-tabs>
  </div>
</template>

<script>
import AppTabs from '@/components/AppTabs.vue';
import GroupMembers from '../components/GroupMembers.vue';
import GroupCategories from '../components/GroupCategories.vue';
import GroupCategoryAlignments from '@/components/GroupCategoryAlignments.vue';
import GroupAudit from '../components/GroupAudit.vue';
import GroupExtended from '@/components/GroupExtended.vue';

export default {
  name: 'group-config-view',
  components: {
    AppTabs,
    GroupMembers,
    GroupCategories,
    GroupCategoryAlignments,
    GroupAudit,
    GroupExtended
  },
  data: () => ({
      tabList: [ "Members", "Categories", "Alignments", "Audit", "More" ]
  })
}
</script>
