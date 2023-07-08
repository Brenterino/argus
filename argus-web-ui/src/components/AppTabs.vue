<template>
  <div class="tabs" :class="{
    flex: variant === 'horizontal',
  }">
    <ul class="tab-header" :class="{
      flex: variant === 'vertical',
    }">
      <li v-for="(tab, index) in tabList" :key="index" class="tab">
        <label :for="`${_uid}${index}`" v-text="tab" class="tab-button" />
        <input :id="`${_uid}${index}`" type="radio" :name="`${_uid}-tab`" :value="index + 1" v-model="activeTab"
          class="tab-button, hidden" />
      </li>
    </ul>

    <template v-for="(tab, index) in tabList">
      <div :key="index" v-if="index + 1 === activeTab">
        <slot :name="`tabPanel-${index + 1}`" />
      </div>
    </template>
  </div>
</template>
  
<script>
export default {
  props: {
    tabList: {
      type: Array,
      required: true,
    },
    variant: {
      type: String,
      required: false,
      default: () => "vertical",
      validator: (value) => ["horizontal", "vertical"].includes(value),
    },
  },
  data() {
    return {
      activeTab: 1,
    };
  },
};
</script>
  
<style>
.flex {
  display: flex;
  justify-content: center;
}

.tab-header {
  padding-inline-start: 0;
  list-style-type: none;
}

.tab {
  margin-left: 2px;
  margin-right: 2px;
  border-radius: 10px;
  border: 2px solid gray;
  padding: 5px;
  min-width: 100px;
}

.tab-button {
  cursor: pointer;
  min-width: 100px;
}

.hidden {
  display: none;
}
</style>
