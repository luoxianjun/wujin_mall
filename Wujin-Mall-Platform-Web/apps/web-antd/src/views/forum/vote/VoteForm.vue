<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import {
  Button,
  Card,
  Image,
  Input,
  message,
  Space,
  Table,
  Textarea,
  Tag,
} from 'ant-design-vue';

import { ImageUpload } from '#/components/upload';

import { requestClient } from '#/api/request';

import {
  auditStatusOptions,
  getVoteActivityOptions,
  useFormSchema,
} from './data';

const emit = defineEmits<{ success: [] }>();
const formData = ref<Record<string, any>>();

/** 投票选项列表 */
const optionsList = ref<
  Array<{
    id?: number;
    title: string;
    imageUrl: string;
    description: string;
    sortOrder: number;
    addedByUser?: boolean;
    auditStatus?: number;
  }>
>([]);

const getTitle = computed(() =>
  formData.value?.id ? '编辑投票活动' : '新增投票活动',
);

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
  commonConfig: { componentProps: { class: 'w-full' } },
});

/** 选项表格列 */
const optionColumns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '配图', dataIndex: 'imageUrl', key: 'imageUrl', width: 140 },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 100 },
  { title: '操作', key: 'action', width: 80 },
];

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      optionsList.value = [];
      await formApi.resetForm();
      return;
    }
    // 加载投票类型活动下拉选项
    try {
      const options = await getVoteActivityOptions();
      formApi.updateSchema([
        {
          fieldName: 'activityId',
          componentProps: { options },
        },
      ]);
    } catch {
      // ignore
    }
    const data = modalApi.getData<Record<string, any>>();
    if (!data?.id) return;
    formData.value = data;
    // endTime: 后端返回 LocalDateTime 序列化值，需确保为时间戳(ms)
    if (data.endTime) {
      const ts = typeof data.endTime === 'number' ? data.endTime
        : new Date(data.endTime).getTime();
      data.endTime = ts > 0 ? ts : null;
    } else {
      data.endTime = null;
    }
    try {
      await formApi.setValues(data);
    } catch (e) {
      console.warn('setValues error', e);
    }
    // 加载已有的选项
    if (data.options && Array.isArray(data.options)) {
      optionsList.value = data.options.map((o: any) => ({
        id: o.id,
        title: o.title || '',
        imageUrl: o.imageUrl || '',
        description: o.description || '',
        sortOrder: o.sortOrder ?? 0,
        addedByUser: o.addedByUser ?? false,
        auditStatus: o.auditStatus ?? 1,
      }));
    }
  },
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;

    // 校验选项
    const adminOptions = optionsList.value.filter((o) => !o.addedByUser);
    if (adminOptions.length < 2) {
      message.error('至少需要2个选项');
      return;
    }
    for (const opt of adminOptions) {
      if (!opt.title.trim()) {
        message.error('选项标题不能为空');
        return;
      }
    }

    modalApi.lock();
    try {
      const values = await formApi.getValues();
      // endTime: DatePicker valueFormat='x' 返回字符串时间戳，后端需要数字
      if (values.endTime) {
        values.endTime = Number(values.endTime);
      }
      // 附加选项（只提交管理员添加的选项）
      values.options = adminOptions.map((o, idx) => ({
        id: o.id,
        title: o.title,
        imageUrl: o.imageUrl,
        description: o.description,
        sortOrder: o.sortOrder ?? idx,
      }));
      if (formData.value?.id) {
        await requestClient.put(
          '/gamification/vote/activity/update',
          values,
        );
        message.success('更新成功');
      } else {
        await requestClient.post(
          '/gamification/vote/activity/create',
          values,
        );
        message.success('创建成功');
      }
      emit('success');
      await modalApi.close();
    } catch (error: any) {
      message.error(error?.message || '操作失败');
    } finally {
      modalApi.unlock();
    }
  },
});

/** 添加选项 */
function handleAddOption() {
  if (optionsList.value.length >= 10) {
    message.warning('选项数量已达上限（10个）');
    return;
  }
  optionsList.value.push({
    title: '',
    imageUrl: '',
    description: '',
    sortOrder: optionsList.value.length,
    addedByUser: false,
    auditStatus: 1,
  });
}

/** 移除选项 */
function handleRemoveOption(index: number) {
  optionsList.value.splice(index, 1);
}

/** 审核用户添加的选项 */
async function handleAuditOption(optionId: number, status: number) {
  try {
    await requestClient.put('/gamification/vote/option/audit', null, {
      params: { optionId, auditStatus: status },
    });
    message.success(status === 1 ? '已通过' : '已拒绝');
    // 更新本地状态
    const opt = optionsList.value.find((o) => o.id === optionId);
    if (opt) opt.auditStatus = status;
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  }
}
</script>

<template>
  <Modal :title="getTitle" class="w-[750px]">
    <Form />

    <Card title="投票选项" size="small" class="mt-4">
      <template #extra>
        <Button size="small" type="primary" @click="handleAddOption">
          添加选项
        </Button>
      </template>

      <Table
        :columns="optionColumns"
        :data-source="optionsList"
        :pagination="false"
        size="small"
        :row-key="(_, index) => index"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'title'">
            <Input
              v-if="!record.addedByUser"
              v-model:value="record.title"
              placeholder="选项标题"
              size="small"
            />
            <span v-else>{{ record.title }}</span>
          </template>
          <template v-else-if="column.key === 'imageUrl'">
            <ImageUpload
              v-if="!record.addedByUser"
              v-model:value="record.imageUrl"
              :max-number="1"
              :multiple="false"
              :show-description="false"
            />
            <Image
              v-else-if="record.imageUrl"
              :src="record.imageUrl"
              :width="60"
              :height="60"
              style="object-fit: cover; border-radius: 4px"
            />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'description'">
            <Input
              v-if="!record.addedByUser"
              v-model:value="record.description"
              placeholder="描述"
              size="small"
            />
            <span v-else>{{ record.description || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'source'">
            <Tag v-if="record.addedByUser" color="blue">用户添加</Tag>
            <Tag v-else color="green">管理员</Tag>
            <template v-if="record.addedByUser">
              <Tag
                v-if="record.auditStatus === 0"
                color="orange"
              >
                待审核
              </Tag>
              <Tag v-else-if="record.auditStatus === 1" color="green">
                已通过
              </Tag>
              <Tag v-else-if="record.auditStatus === 2" color="red">
                已拒绝
              </Tag>
            </template>
          </template>
          <template v-else-if="column.key === 'action'">
            <Space v-if="record.addedByUser && record.id">
              <Button
                v-if="record.auditStatus !== 1"
                size="small"
                type="link"
                @click="handleAuditOption(record.id, 1)"
              >
                通过
              </Button>
              <Button
                v-if="record.auditStatus !== 2"
                danger
                size="small"
                type="link"
                @click="handleAuditOption(record.id, 2)"
              >
                拒绝
              </Button>
            </Space>
            <Button
              v-else-if="!record.addedByUser"
              danger
              size="small"
              type="link"
              @click="handleRemoveOption(index)"
            >
              移除
            </Button>
          </template>
        </template>
      </Table>
    </Card>
  </Modal>
</template>
