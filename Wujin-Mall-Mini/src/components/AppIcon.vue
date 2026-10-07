<template>
  <image
    class="app-icon"
    :src="iconSrc"
    :mode="mode"
    :style="iconStyle"
    @click="handleClick"
  />
</template>

<script>
import { getAppIconPath } from "@/utils/appIcons";

export default {
  name: "AppIcon",
  emits: ["click"],
  props: {
    name: {
      type: String,
      default: "",
    },
    variant: {
      type: String,
      default: "default",
    },
    size: {
      type: [String, Number],
      default: "1em",
    },
    width: {
      type: [String, Number],
      default: "",
    },
    height: {
      type: [String, Number],
      default: "",
    },
    mode: {
      type: String,
      default: "aspectFit",
    },
  },
  computed: {
    iconSrc() {
      return getAppIconPath(this.name, this.variant);
    },
    iconStyle() {
      const baseSize = this.normalizeSize(this.size || "1em");
      const width = this.width ? this.normalizeSize(this.width) : baseSize;
      const height = this.height ? this.normalizeSize(this.height) : width;

      return {
        width,
        height,
      };
    },
  },
  methods: {
    handleClick(event) {
      this.$emit("click", event);
    },
    normalizeSize(value) {
      if (typeof value === "number") {
        return `${value}rpx`;
      }
      return value || "1em";
    },
  },
};
</script>

<style scoped>
.app-icon {
  display: inline-block;
  flex-shrink: 0;
  vertical-align: middle;
}
</style>
