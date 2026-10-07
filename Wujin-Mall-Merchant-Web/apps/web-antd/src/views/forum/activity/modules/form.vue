<script lang="ts" setup>
import type { ForumActivityApi } from '#/api/forum/activity';
import type { ForumUserProfileApi } from '#/api/forum/userProfile';
import type { SelectProps } from 'ant-design-vue';

import dayjs from 'dayjs';
import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';
import { useDebounceFn } from '@vueuse/core';

import { message } from 'ant-design-vue';

import {
  createActivity,
  getActivity,
  updateActivity,
} from '#/api/forum/activity';
import { WangEditor } from '#/components/wang-editor';
import {
  getForumUserProfilePage,
  getForumUserProfileByUserId,
} from '#/api/forum/userProfile';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

defineOptions({ name: 'ForumActivityForm' });

const emit = defineEmits(['success']);
const formData = ref<ForumActivityApi.Activity>();
const adminMemberOptions = ref<SelectProps['options']>([]);
const adminMemberSearch = ref('');
const loadingAdminMembers = ref(false);
/** 活动描述（手动管理，绕过 Vben Form 的组件问题） */
const descriptionValue = ref('');
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['活动'])
    : $t('ui.actionTitle.create', ['活动']);
});

function normalizeNumber(value: unknown) {
  if (value === undefined || value === null || value === '') return undefined;
  const num = Number(value);
  return Number.isNaN(num) ? undefined : num;
}

function formatDateValue(value: unknown) {
  if (value === undefined || value === null || value === '') return undefined;
  if (typeof value === 'number') {
    return dayjs(value).format('YYYY-MM-DD HH:mm:ss');
  }
  // 字符串保持，交给 DatePicker 的 valueFormat 处理
  return value as string;
}

// 解析自定义字段 JSON 字符串为数组
function parseCustomFields(value: unknown): unknown[] {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  return [];
}

function formatMemberOption(user: ForumUserProfileApi.AdminUserProfileRespVO) {
  const label = user.nickname
    ? `${user.nickname}${user.uid ? ` (UID: ${user.uid})` : ''}`
    : user.uid
      ? `UID: ${user.uid}`
      : user.userId
        ? `ID: ${user.userId}`
        : '-';
  return {
    label,
    value: user.userId as number,
  };
}

async function fetchAdminMemberOptions(keyword?: string) {
  loadingAdminMembers.value = true;
  try {
    const trimmedKeyword = keyword?.trim();
    // 判断是否为 UID 格式：以 U 开头才认为是 UID（纯数字使用昵称搜索，因为 UID 通常是 U 开头）
    const isUidFormat = trimmedKeyword && trimmedKeyword.startsWith('U');
    const params: ForumUserProfileApi.AdminUserProfilePageReqVO = {
      pageNo: 1,
      pageSize: 20,
    };
    if (trimmedKeyword) {
      if (isUidFormat) {
        params.uid = trimmedKeyword;
      } else {
        // 纯数字或其他文本都使用昵称模糊搜索
        params.nickname = trimmedKeyword;
      }
    }
    const { list } = await getForumUserProfilePage(params);
    const nextOptions = (list ?? [])
      .filter((item) => item.userId !== undefined && item.userId !== null)
      .map((item) => formatMemberOption(item));
    
    // 如果有搜索关键词，直接使用搜索结果；否则合并保留已选择的选项
    if (trimmedKeyword) {
      // 有搜索关键词时，替换选项列表，但保留已选择的选项（如果不在搜索结果中）
      const selectedValues = formApi.getValues()?.adminMemberIds || [];
      const selectedOptions = selectedValues
        .map((value: number) => adminMemberOptions.value?.find((opt) => opt?.value === value))
        .filter((opt) => opt != null);
      
      const optionsToAdd = selectedOptions.filter(
        (opt) => !nextOptions.some((item) => item.value === opt?.value),
      );
      if (optionsToAdd.length > 0) {
        adminMemberOptions.value = [...optionsToAdd, ...nextOptions];
      } else {
        adminMemberOptions.value = nextOptions;
      }
    } else {
      // 没有搜索关键词时，合并保留已存在的选项
      const mergedOptions = [...nextOptions];
      for (const option of adminMemberOptions.value ?? []) {
        if (
          option &&
          option.value !== undefined &&
          !mergedOptions.some((item) => item?.value === option.value)
        ) {
          mergedOptions.push(option);
        }
      }
      adminMemberOptions.value = mergedOptions;
    }
  } finally {
    loadingAdminMembers.value = false;
  }
}

const debouncedSearchMembers = useDebounceFn(
  async (keyword: string) => {
    await fetchAdminMemberOptions(keyword);
  },
  300,
);

async function ensureAdminMemberOptions(adminIds?: number[]) {
  if (!adminIds || adminIds.length === 0) return;
  const missingIds = adminIds.filter(
    (id) => !adminMemberOptions.value?.some((opt) => opt?.value === id),
  );
  if (missingIds.length === 0) return;
  
  try {
    const promises = missingIds.map((id) => getForumUserProfileByUserId(id));
    const users = await Promise.all(promises);
    const newOptions = users
      .filter((user) => user?.userId)
      .map((user) => formatMemberOption(user));
    adminMemberOptions.value = [
      ...newOptions,
      ...(adminMemberOptions.value ?? []),
    ];
  } catch (error) {
    console.warn('加载活动管理员信息失败', error);
  }
}

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 110,
  },
  wrapperClass: 'grid-cols-2',
  layout: 'horizontal',
  schema: useFormSchema({
    getAdminMemberSelectProps: () => ({
      options: adminMemberOptions.value,
      loading: loadingAdminMembers.value,
      onDropdownVisibleChange: (open: boolean) => {
        if (open && adminMemberOptions.value.length === 0) {
          void fetchAdminMemberOptions(adminMemberSearch.value);
        }
      },
      onSearch: (value: string) => {
        adminMemberSearch.value = value;
        void debouncedSearchMembers(value);
      },
    }),
  }),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    // 验证活动描述
    if (!descriptionValue.value || descriptionValue.value === '<p><br></p>') {
      message.error('请输入活动描述');
      return;
    }
    modalApi.lock();
    const values = await formApi.getValues();
    const payload: ForumActivityApi.Activity = {
      ...values,
      description: descriptionValue.value,
      id: normalizeNumber(values.id) as number | undefined,
      category: normalizeNumber(values.category),
      longitude:
        values.checkInType === 2
          ? (normalizeNumber(values.longitude) as number | undefined)
          : undefined,
      latitude:
        values.checkInType === 2
          ? (normalizeNumber(values.latitude) as number | undefined)
          : undefined,
      checkInDistance:
        values.checkInType === 2
          ? (normalizeNumber(values.checkInDistance ?? 100) as number | undefined)
          : undefined,
      maxParticipants: normalizeNumber(values.maxParticipants),
      requirements: values.requirements as string | undefined,
      startTime: values.startTime as string | undefined,
      endTime: values.endTime as string | undefined,
      signUpStartTime: values.signUpStartTime as string | undefined,
      signUpEndTime: values.signUpEndTime as string | undefined,
      checkInStartTime: values.checkInStartTime as string | undefined,
      checkInEndTime: values.checkInEndTime as string | undefined,
      checkInType: normalizeNumber(values.checkInType),
      adminMemberIds: values.adminMemberIds,
      hot:
        values.hot === undefined
          ? undefined
          : (values.hot as boolean) ? 1 : 0,
      needPoint: values.needPoint as boolean | undefined,
      pointAmount: values.needPoint
        ? normalizeNumber(values.pointAmount)
        : undefined,
      allowUnverified: values.allowUnverified as boolean | undefined,
      showParticipantCount: values.showParticipantCount as boolean | undefined,
      // 将数组转换为 JSON 字符串，空数组转换为 undefined
      customFields: Array.isArray(values.customFields)
        ? (values.customFields.length > 0 ? JSON.stringify(values.customFields) : undefined)
        : (values.customFields as string | undefined),
    };
    try {
      await (formData.value?.id ? updateActivity(payload) : createActivity(payload));
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      adminMemberSearch.value = '';
      adminMemberOptions.value = [];
      descriptionValue.value = '';
      return;
    }
    const data = modalApi.getData<ForumActivityApi.Activity>();
    if (!data || !data.id) {
      adminMemberSearch.value = '';
      adminMemberOptions.value = [];
      descriptionValue.value = '';
      await fetchAdminMemberOptions();
      await formApi.resetForm();
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getActivity(data.id);
      if (formData.value) {
        adminMemberSearch.value = '';
        descriptionValue.value = formData.value.description || '';
        await fetchAdminMemberOptions();
        await ensureAdminMemberOptions(formData.value.adminMemberIds);
        await formApi.setValues({
          ...formData.value,
          startTime: formatDateValue(formData.value.startTime),
          endTime: formatDateValue(formData.value.endTime),
          signUpStartTime: formatDateValue(formData.value.signUpStartTime),
          signUpEndTime: formatDateValue(formData.value.signUpEndTime),
          checkInStartTime: formatDateValue(formData.value.checkInStartTime),
          checkInEndTime: formatDateValue(formData.value.checkInEndTime),
          checkInType: formData.value.checkInType,
          longitude:
            formData.value.checkInType === 2
              ? normalizeNumber(formData.value.longitude)
              : undefined,
          latitude:
            formData.value.checkInType === 2
              ? normalizeNumber(formData.value.latitude)
              : undefined,
          checkInDistance:
            formData.value.checkInType === 2
              ? normalizeNumber(formData.value.checkInDistance ?? 100)
              : undefined,
          hot: formData.value.hot === 1 || formData.value.hot === true,
          needPoint:
            formData.value.needPoint === 1 || formData.value.needPoint === true,
          allowUnverified:
            formData.value.allowUnverified === 1 || formData.value.allowUnverified === true || formData.value.allowUnverified === undefined,
          showParticipantCount:
            formData.value.showParticipantCount === undefined || formData.value.showParticipantCount === null
              ? true
              : (formData.value.showParticipantCount === 1 || formData.value.showParticipantCount === true),
          // 将 JSON 字符串解析为数组
          customFields: parseCustomFields(formData.value.customFields),
        });
      }
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-3/5" :title="getTitle">
    <Form>
      <!-- 活动描述使用 slot 自定义渲染，绕过 Vben Form 组件绑定问题 -->
      <template #description="{ disabled }">
        <WangEditor
          v-model:value="descriptionValue"
          :height="400"
          :disabled="disabled"
          placeholder="请输入活动描述"
        />
      </template>
    </Form>
  </Modal>
</template>
