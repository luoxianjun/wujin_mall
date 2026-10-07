import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { formatDate } from '@vben/utils';

import { z } from '#/adapter/form';
import { DICT_TYPE, getDictLabel, getDictOptions } from '#/utils';

const quizStatusLabelMap: Record<string, string> = {
  DISABLED: '已停用',
  DRAFT: '草稿',
  ENABLED: '已启用',
};

/** 活动状态选项（后端返回 statusName，前端仅提供简单筛选编号） */
export const activityStatusOptions = [
  { label: '全部状态', value: null },
  { label: '报名中', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已结束', value: 3 },
  { label: '已取消', value: 4 },
];

export const hiddenStatusOptions = [
  { label: '全部', value: null },
  { label: '已隐藏', value: true },
  { label: '展示中', value: false },
];

export const checkInTypeOptions = [
  { label: '自助签到', value: 1 },
  { label: '定位签到', value: 2 },
  { label: '扫码签到', value: 3 },
];

/** 自定义字段类型选项 */
export const customFieldTypeOptions = [
  { label: '输入框', value: 'input' },
  { label: '文本域', value: 'textarea' },
  { label: '单选', value: 'radio' },
  { label: '多选', value: 'checkbox' },
  { label: '下拉选择', value: 'select' },
  { label: '日期', value: 'date' },
  { label: '上传文件', value: 'file' },
];

/** 自定义字段配置接口 */
export interface CustomField {
  key: string;
  label: string;
  type: 'checkbox' | 'date' | 'file' | 'input' | 'radio' | 'select' | 'textarea';
  required?: boolean;
  placeholder?: string;
  options?: string[];
}

export interface ActivityFormSchemaOptions {
  getAdminMemberSelectProps?: () => Record<string, any>;
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'keyword',
      label: '关键字',
      component: 'Input',
      componentProps: {
        placeholder: '标题/描述模糊搜索',
        clearable: true,
      },
    },
    {
      fieldName: 'category',
      label: '分类',
      component: 'Select',
      componentProps: {
        placeholder: '请选择分类',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.FRUM_ACTIVITY_TYPE, 'number'),
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        allowClear: true,
        options: activityStatusOptions,
      },
    },
    {
      fieldName: 'hidden',
      label: '隐藏状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择隐藏状态',
        allowClear: true,
        options: hiddenStatusOptions,
      },
    },
    {
      fieldName: 'checkInType',
      label: '签到方式',
      component: 'Select',
      componentProps: {
        placeholder: '请选择签到方式',
        allowClear: true,
        options: checkInTypeOptions,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: 'ID',
      minWidth: 80,
    },
    {
      field: 'title',
      title: '活动标题',
      minWidth: 160,
      showOverflow: 'tooltip',
    },
    {
      field: 'categoryName',
      title: '分类',
      minWidth: 120,
      formatter: ({ row }) =>
        getDictLabel(DICT_TYPE.FRUM_ACTIVITY_TYPE, row.category) ||
        row.categoryName ||
        row.category ||
        '-',
    },
    {
      field: 'statusName',
      title: '状态',
      minWidth: 110,
      formatter: ({ row }) => row.statusName || row.status || '-',
    },
    {
      field: 'hasQuiz',
      title: '答题活动',
      minWidth: 120,
      formatter: ({ row }) =>
        row.hasQuiz
          ? quizStatusLabelMap[row.quizStatus || 'ENABLED'] ||
            row.quizStatus ||
            '已启用'
          : '未配置',
    },
    {
      field: 'checkInType',
      title: '签到方式',
      minWidth: 110,
      formatter: ({ row }) => {
        const found = checkInTypeOptions.find(
          (i) => i.value === row.checkInType,
        );
        return found?.label || row.checkInType || '-';
      },
    },
    {
      field: 'needApproval',
      title: '需审核',
      minWidth: 90,
      formatter: ({ row }) => (row.needApproval ? '是' : '否'),
    },
    {
      field: 'needPoint',
      title: '报名奖励积分',
      minWidth: 90,
      formatter: ({ row }) => (row.needPoint ? '是' : '否'),
    },
    {
      field: 'pointAmount',
      title: '奖励积分',
      minWidth: 110,
      formatter: ({ row }) => {
        if (!row.needPoint) return '-';
        const amount = row.pointAmount ?? 0;
        return amount > 0 ? amount : '0';
      },
    },
    {
      field: 'hot',
      title: '热点',
      minWidth: 80,
      formatter: ({ row }) => (row.hot ? '是' : '否'),
    },
    {
      field: 'hidden',
      title: '隐藏',
      minWidth: 80,
      formatter: ({ row }) => (row.hidden ? '是' : '否'),
    },
    {
      field: 'activityTime',
      title: '活动时间',
      minWidth: 220,
      formatter: ({ row }) => {
        if (!row.startTime || !row.endTime) return '-';
        return `${formatDate(row.startTime, 'YYYY-MM-DD HH:mm')} ~ ${formatDate(row.endTime, 'YYYY-MM-DD HH:mm')}`;
      },
    },
    {
      field: 'currentParticipants',
      title: '报名/上限',
      minWidth: 140,
      formatter: ({ row }) => {
        // 如果设置了不显示报名人数，则不显示此列内容
        if (row.showParticipantCount === false) {
          return '-';
        }
        const upper = row.maxParticipants ?? 0;
        return `${row.currentParticipants ?? 0}/${upper === 0 ? '不限' : upper}`;
      },
    },
    {
      field: 'viewCount',
      title: '浏览',
      minWidth: 80,
    },
    {
      field: 'likeCount',
      title: '点赞',
      minWidth: 80,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 360,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 表单配置 */
export function useFormSchema(
  options: ActivityFormSchemaOptions = {},
): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'title',
      label: '活动标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动标题',
      },
      rules: 'required',
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'requirements',
      label: '报名要求',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入报名要求，如资格条件、需携带物品等',
        rows: 3,
      },
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'description',
      label: '活动描述',
      // 使用 slot 自定义渲染，此处仅作为占位
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'coverImage',
      label: '活动封面',
      component: 'ImageUpload',
      componentProps: {
        maxNumber: 1,
        multiple: false,
        helpText: '支持 jpg/png，单张上传',
      },
      rules: 'required',
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'category',
      label: '活动分类',
      component: 'Select',
      componentProps: {
        placeholder: '请选择分类',
        options: getDictOptions(DICT_TYPE.FRUM_ACTIVITY_TYPE, 'number'),
      },
      rules: 'required',
    },
    {
      fieldName: 'adminMemberIds',
      label: '活动管理员',
      component: 'Select',
      componentProps: () => ({
        placeholder: '请选择活动管理员',
        allowClear: true,
        showSearch: true,
        filterOption: false,
        mode: 'multiple',
        ...options.getAdminMemberSelectProps?.(),
      }),
      rules: 'required',
    },
    {
      fieldName: 'location',
      label: '活动地点',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动地点',
      },
      rules: 'required',
    },
    {
      fieldName: 'startTime',
      label: '开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择开始时间',
      },
      rules: 'required',
    },
    {
      fieldName: 'endTime',
      label: '结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择结束时间',
      },
      rules: 'required',
    },
    {
      fieldName: 'signUpStartTime',
      label: '报名开始',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '可选',
      },
    },
    {
      fieldName: 'signUpEndTime',
      label: '报名结束',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '可选',
      },
    },
    {
      fieldName: 'checkInStartTime',
      label: '签到开始',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '可选',
      },
    },
    {
      fieldName: 'checkInEndTime',
      label: '签到结束',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '可选',
      },
    },
    {
      fieldName: 'checkInType',
      label: '签到方式',
      component: 'Select',
      componentProps: {
        options: checkInTypeOptions,
        placeholder: '请选择签到方式',
      },
      defaultValue: 1,
      rules: 'required',
    },
    {
      fieldName: 'checkInDistance',
      label: '签到距离（米）',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, placeholder: '默认 100 米' },
      dependencies: {
        triggerFields: ['checkInType'],
        show: (values) => values.checkInType === 2,
        rules: (values) => {
          if (values.checkInType !== 2) return undefined;
          return z
            .number({ message: '签到距离不能为空' })
            .nonnegative('签到距离不能小于 0');
        },
      },
      defaultValue: 100,
    },
    {
      fieldName: 'longitude',
      label: '经度',
      component: 'InputNumber',
      componentProps: { class: 'w-full', placeholder: '请填写经度' },
      dependencies: {
        triggerFields: ['checkInType'],
        show: (values) => values.checkInType === 2,
        rules: (values) =>
          values.checkInType === 2
            ? z.number({ message: '经度不能为空' })
            : undefined,
      },
    },
    {
      fieldName: 'latitude',
      label: '纬度',
      component: 'InputNumber',
      componentProps: { class: 'w-full', placeholder: '请填写纬度' },
      dependencies: {
        triggerFields: ['checkInType'],
        show: (values) => values.checkInType === 2,
        rules: (values) =>
          values.checkInType === 2
            ? z.number({ message: '纬度不能为空' })
            : undefined,
      },
    },
    {
      fieldName: 'maxParticipants',
      label: '人数上限',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, placeholder: '0 表示不限制' },
    },
    {
      fieldName: 'showParticipantCount',
      label: '显示报名人数',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
        class: '!w-auto',
      },
      defaultValue: true,
    },
    {
      fieldName: 'needApproval',
      label: '报名需审核',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
        class: '!w-auto',
      },
      defaultValue: false,
    },
    {
      fieldName: 'needPoint',
      label: '报名奖励积分',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
        class: '!w-auto',
      },
      defaultValue: false,
    },
    {
      fieldName: 'pointAmount',
      label: '奖励积分',
      component: 'InputNumber',
      componentProps: {
        class: 'w-full',
        min: 0,
        placeholder: '请输入奖励积分',
      },
      dependencies: {
        triggerFields: ['needPoint'],
        show: (values) => !!values.needPoint,
        rules: (values) => {
          if (!values.needPoint) return undefined;
          return z.number().nonnegative('积分不能小于 0').default(0);
        },
      },
      helpMessage: '用户报名成功后立即获得的积分奖励',
    },
    {
      fieldName: 'schoolOnly',
      label: '仅本校可见',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
        class: '!w-auto',
      },
      defaultValue: false,
    },
    {
      fieldName: 'allowUnverified',
      label: '允许未实名用户报名',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
        class: '!w-auto',
      },
      defaultValue: true,
    },
    {
      fieldName: 'hot',
      label: '热点',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
        class: '!w-auto',
      },
      defaultValue: false,
    },
    {
      fieldName: 'hidden',
      label: '隐藏活动',
      component: 'Switch',
      componentProps: {
        checkedChildren: '隐藏',
        unCheckedChildren: '展示',
        class: '!w-auto',
      },
      defaultValue: false,
      helpMessage:
        '隐藏后用户将无法在活动列表中看到该活动，但已报名的用户仍可查看',
    },
    {
      fieldName: 'customFields',
      label: '自定义报名字段',
      component: 'CustomFieldEditor',
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'redirectAppId',
      label: '跳转小程序AppId',
      component: 'Input',
      componentProps: {
        placeholder: '如需跳转其他小程序，请填写目标小程序的AppId',
      },
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'redirectAppPath',
      label: '跳转页面路径',
      component: 'Input',
      componentProps: {
        placeholder: '目标小程序的页面路径，如 pages/index/index',
      },
      dependencies: {
        triggerFields: ['redirectAppId'],
        show: (values) => !!values.redirectAppId,
      },
    },
    {
      fieldName: 'redirectAppName',
      label: '跳转按钮文案',
      component: 'Input',
      componentProps: {
        placeholder: '小程序底部按钮显示的文案，如"打开XX小程序"',
      },
      dependencies: {
        triggerFields: ['redirectAppId'],
        show: (values) => !!values.redirectAppId,
      },
    },
  ];
}
