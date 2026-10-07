<script lang="ts" setup>
import type { FormInstance } from 'ant-design-vue';

import type { InvitationApi } from '#/api/gamification/invitation';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Form,
  FormItem,
  InputNumber,
  message,
  Select,
  Switch,
} from 'ant-design-vue';

import {
  getInvitationConfig,
  updateInvitationConfig,
} from '#/api/gamification/invitation';

defineOptions({ name: 'InvitationConfig' });

const loading = ref(false);
const formRef = ref<FormInstance>();
const rewardModeOptions: Array<{
  label: string;
  value: 'both' | 'inviter_only';
}> = [
  { label: '双方奖励', value: 'both' },
  { label: '仅奖励邀请人', value: 'inviter_only' },
];

const formData = ref<InvitationApi.InvitationConfigUpdateReq>({
  id: 1,
  inviterRewardPoints: 20,
  inviteeRewardPoints: 10,
  rewardMode: 'both',
  maxInvitations: 100,
  enabled: true,
  rewardRetryLimit: 3,
  ipLimit: 5,
  deviceLimit: 3,
});

const rules: Record<string, any> = {
  inviterRewardPoints: [
    { required: true, message: '请输入邀请人奖励积分', trigger: 'blur' },
  ],
  inviteeRewardPoints: [
    { required: true, message: '请输入被邀请人奖励积分', trigger: 'blur' },
  ],
  rewardMode: [
    { required: true, message: '请选择奖励模式', trigger: 'change' },
  ],
  maxInvitations: [
    { required: true, message: '请输入最大邀请次数', trigger: 'blur' },
  ],
  rewardRetryLimit: [
    { required: true, message: '请输入奖励重试上限', trigger: 'blur' },
  ],
  ipLimit: [{ required: true, message: '请输入 IP 限制', trigger: 'blur' }],
  deviceLimit: [{ required: true, message: '请输入设备限制', trigger: 'blur' }],
};

async function loadConfig() {
  loading.value = true;
  try {
    const res = await getInvitationConfig();
    formData.value = {
      id: res.id || 1,
      inviterRewardPoints: res.inviterRewardPoints ?? 20,
      inviteeRewardPoints: res.inviteeRewardPoints ?? 10,
      rewardMode: res.rewardMode ?? 'both',
      maxInvitations: res.maxInvitations ?? 100,
      enabled: res.enabled ?? true,
      rewardRetryLimit: res.rewardRetryLimit ?? 3,
      ipLimit: res.ipLimit ?? 5,
      deviceLimit: res.deviceLimit ?? 3,
    };
  } catch {
    message.error('加载邀请配置失败');
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    await updateInvitationConfig(formData.value);
    message.success('邀请配置已保存');
    await loadConfig();
  } catch {
    message.error('保存邀请配置失败');
  } finally {
    loading.value = false;
  }
}

function handleReset() {
  void loadConfig();
}

onMounted(() => {
  void loadConfig();
});
</script>

<template>
  <Page
    auto-content-height
    content-class="flex flex-col gap-4"
    description="管理邀请奖励、重试策略和防作弊限制。"
    title="邀请配置"
  >
    <Card :loading="loading" title="邀请设置">
      <Form
        ref="formRef"
        :label-col="{ span: 7 }"
        :model="formData"
        :rules="rules"
        :wrapper-col="{ span: 11 }"
      >
        <FormItem label="邀请人奖励积分" name="inviterRewardPoints">
          <InputNumber
            v-model:value="formData.inviterRewardPoints"
            :min="0"
            :step="1"
            style="width: 100%"
          />
        </FormItem>

        <FormItem label="被邀请人奖励积分" name="inviteeRewardPoints">
          <InputNumber
            v-model:value="formData.inviteeRewardPoints"
            :min="0"
            :step="1"
            style="width: 100%"
          />
        </FormItem>

        <FormItem label="奖励模式" name="rewardMode">
          <Select
            v-model:value="formData.rewardMode"
            :options="rewardModeOptions"
          />
        </FormItem>

        <FormItem label="最大邀请次数" name="maxInvitations">
          <InputNumber
            v-model:value="formData.maxInvitations"
            :min="1"
            :step="1"
            style="width: 100%"
          />
        </FormItem>

        <FormItem label="奖励重试上限" name="rewardRetryLimit">
          <InputNumber
            v-model:value="formData.rewardRetryLimit"
            :min="0"
            :step="1"
            style="width: 100%"
          />
        </FormItem>

        <FormItem label="IP 限制" name="ipLimit">
          <InputNumber
            v-model:value="formData.ipLimit"
            :min="1"
            :step="1"
            style="width: 100%"
          />
        </FormItem>

        <FormItem label="设备限制" name="deviceLimit">
          <InputNumber
            v-model:value="formData.deviceLimit"
            :min="1"
            :step="1"
            style="width: 100%"
          />
        </FormItem>

        <FormItem label="启用状态" name="enabled">
          <Switch v-model:checked="formData.enabled" />
        </FormItem>

        <FormItem :wrapper-col="{ offset: 7, span: 11 }">
          <Button :loading="loading" type="primary" @click="handleSave">
            保存
          </Button>
          <Button :loading="loading" class="ml-2" @click="handleReset">
            重置
          </Button>
        </FormItem>
      </Form>
    </Card>
  </Page>
</template>
