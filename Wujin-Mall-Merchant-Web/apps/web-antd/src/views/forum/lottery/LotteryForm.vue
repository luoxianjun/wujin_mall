<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import {
  Button,
  Card,
  InputNumber,
  message,
  Select,
  Space,
  Table,
} from 'ant-design-vue';

import { requestClient } from '#/api/request';

import {
  getLotteryActivityOptions,
  getPrizeSimpleList,
  prizeTypeOptions,
  useFormSchema,
} from './data';

const emit = defineEmits<{ success: [] }>();
const formData = ref<Record<string, any>>();

/** 奖品关联配置 */
const prizeOptions = ref<Array<{ label: string; value: number }>>([]);
const selectedPrizes = ref<
  Array<{ prizeId: number; probability: number; sortOrder: number }>
>([]);

const getTitle = computed(() =>
  formData.value?.id ? '编辑抽奖活动' : '新增抽奖活动',
);

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
  commonConfig: { componentProps: { class: 'w-full' } },
});

/** 奖品选择表格列 */
const prizeColumns = [
  { title: '奖品', dataIndex: 'prizeName', key: 'prizeName' },
  {
    title: '概率(%)',
    dataIndex: 'probability',
    key: 'probability',
    width: 120,
  },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 100 },
  { title: '操作', key: 'action', width: 80 },
];

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      selectedPrizes.value = [];
      await formApi.resetForm();
      return;
    }
    // 加载抽奖类型活动下拉选项
    try {
      const options = await getLotteryActivityOptions();
      formApi.updateSchema([
        {
          fieldName: 'activityId',
          componentProps: { options },
        },
      ]);
    } catch {
      // ignore
    }
    // 加载奖品列表
    try {
      prizeOptions.value = await getPrizeSimpleList();
    } catch {
      // ignore
    }
    const data = modalApi.getData<Record<string, any>>();
    if (!data?.id) return;
    formData.value = data;
    // drawTime=0 表示未设置（后端序列化 null 为 0），置为 null 让 DatePicker 显示为空
    if (data.drawTime === 0) {
      data.drawTime = null;
    }
    await formApi.setValues(data);
    // 加载已关联的奖品
    if (data.prizes && Array.isArray(data.prizes)) {
      selectedPrizes.value = data.prizes.map((p: any) => ({
        prizeId: p.prizeId,
        prizeName:
          prizeOptions.value.find((o) => o.value === p.prizeId)?.label ||
          `奖品#${p.prizeId}`,
        probability: p.probability,
        sortOrder: p.sortOrder ?? 0,
      }));
    }
  },
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;

    // 校验奖品概率
    if (selectedPrizes.value.length > 0) {
      const totalProb = selectedPrizes.value.reduce(
        (sum, p) => sum + (p.probability || 0),
        0,
      );
      if (Math.abs(totalProb - 100) > 0.01) {
        message.error(
          `奖品概率之和必须为 100%，当前为 ${totalProb.toFixed(2)}%`,
        );
        return;
      }
    }

    modalApi.lock();
    try {
      const values = await formApi.getValues();
      // drawTime: DatePicker valueFormat='x' 返回字符串时间戳，后端需要数字
      if (values.drawTime) {
        values.drawTime = Number(values.drawTime);
      }
      // 附加奖品关联
      values.prizes = selectedPrizes.value.map((p, idx) => ({
        prizeId: p.prizeId,
        probability: p.probability,
        sortOrder: p.sortOrder ?? idx,
      }));
      if (formData.value?.id) {
        await requestClient.put(
          '/gamification/lottery/activity/update',
          values,
        );
        message.success('更新成功');
      } else {
        await requestClient.post(
          '/gamification/lottery/activity/create',
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

/** 添加奖品 */
const addPrizeId = ref<number>();

function handleAddPrize() {
  if (!addPrizeId.value) return;
  if (selectedPrizes.value.some((p) => p.prizeId === addPrizeId.value)) {
    message.warning('该奖品已添加');
    return;
  }
  const opt = prizeOptions.value.find((o) => o.value === addPrizeId.value);
  selectedPrizes.value.push({
    prizeId: addPrizeId.value,
    prizeName: opt?.label || `奖品#${addPrizeId.value}`,
    probability: 0,
    sortOrder: selectedPrizes.value.length,
  } as any);
  addPrizeId.value = undefined;
}

function handleRemovePrize(index: number) {
  selectedPrizes.value.splice(index, 1);
}
</script>

<template>
  <Modal :title="getTitle" class="w-[700px]">
    <Form />

    <Card title="奖品配置" size="small" class="mt-4">
      <template #extra>
        <Space>
          <Select
            v-model:value="addPrizeId"
            :options="prizeOptions"
            placeholder="选择奖品"
            show-search
            :filter-option="
              (input: string, option: any) =>
                (option?.label ?? '')
                  .toLowerCase()
                  .includes(input.toLowerCase())
            "
            style="width: 240px"
          />
          <Button size="small" type="primary" @click="handleAddPrize">
            添加
          </Button>
        </Space>
      </template>

      <Table
        :columns="prizeColumns"
        :data-source="selectedPrizes"
        :pagination="false"
        size="small"
        row-key="prizeId"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'probability'">
            <InputNumber
              v-model:value="record.probability"
              :min="0"
              :max="100"
              :step="0.01"
              size="small"
              style="width: 100%"
            />
          </template>
          <template v-else-if="column.key === 'sortOrder'">
            <InputNumber
              v-model:value="record.sortOrder"
              :min="0"
              size="small"
              style="width: 100%"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <Button
              danger
              size="small"
              type="link"
              @click="handleRemovePrize(index)"
            >
              移除
            </Button>
          </template>
        </template>
      </Table>
    </Card>
  </Modal>
</template>
