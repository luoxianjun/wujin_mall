import {
	createSSRApp
} from "vue";
import App from "./App.vue";
import AppIcon from "@/components/AppIcon.vue";
import toast from "@/utils/toast";
import defaultShareMixin from "@/mixins/defaultShare";

export function createApp() {
	const app = createSSRApp(App);

	app.component("AppIcon", AppIcon);
	app.mixin(defaultShareMixin);

	// 注册全局 toast 方法
	app.config.globalProperties.$toast = toast;

	return {
		app,
	};
}
