<template>
  <GroupMembers />
  <GroupConfig />
</template>

<script>
import GroupConfig from '../components/GroupConfig.vue';
import GroupMembers from '../components/GroupMembers.vue';

export default {
  name: 'group-view',
  components: {
    GroupConfig,
    GroupMembers
  }
}
</script>
