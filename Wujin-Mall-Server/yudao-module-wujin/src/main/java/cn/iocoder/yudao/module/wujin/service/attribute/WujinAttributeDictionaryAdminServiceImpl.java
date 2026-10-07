package cn.iocoder.yudao.module.wujin.service.attribute;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionaryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionarySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinAttributeDictionaryDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.attribute.WujinAttributeDictionaryMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Validated
public class WujinAttributeDictionaryAdminServiceImpl implements WujinAttributeDictionaryAdminService {

    public static final int STATUS_ENABLED = 0;
    public static final String VALUE_TYPE_TEXT = "TEXT";
    public static final String VALUE_TYPE_NUMBER = "NUMBER";
    public static final String VALUE_TYPE_ENUM = "ENUM";
    public static final String VALUE_TYPE_MULTI_ENUM = "MULTI_ENUM";
    public static final String VALUE_TYPE_BOOLEAN = "BOOLEAN";

    private static final List<String> VALUE_TYPES = Arrays.asList(VALUE_TYPE_TEXT, VALUE_TYPE_NUMBER,
            VALUE_TYPE_ENUM, VALUE_TYPE_MULTI_ENUM, VALUE_TYPE_BOOLEAN);

    @Resource
    private WujinAttributeDictionaryMapper attributeDictionaryMapper;

    @Override
    public Long createAttribute(WujinAttributeDictionarySaveReqVO createReqVO) {
        normalize(createReqVO);
        validateCodeUnique(null, createReqVO.getCode());
        WujinAttributeDictionaryDO attribute = BeanUtils.toBean(createReqVO, WujinAttributeDictionaryDO.class);
        attributeDictionaryMapper.insert(attribute);
        return attribute.getId();
    }

    @Override
    public void updateAttribute(WujinAttributeDictionarySaveReqVO updateReqVO) {
        validateAttributeExists(updateReqVO.getId());
        normalize(updateReqVO);
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
        WujinAttributeDictionaryDO attribute = BeanUtils.toBean(updateReqVO, WujinAttributeDictionaryDO.class);
        attributeDictionaryMapper.updateById(attribute);
    }

    @Override
    public void deleteAttribute(Long id) {
        validateAttributeExists(id);
        attributeDictionaryMapper.deleteById(id);
    }

    @Override
    public WujinAttributeDictionaryDO getAttribute(Long id) {
        return attributeDictionaryMapper.selectById(id);
    }

    @Override
    public List<WujinAttributeDictionaryDO> getAttributeList(WujinAttributeDictionaryListReqVO listReqVO) {
        return attributeDictionaryMapper.selectList(listReqVO);
    }

    @Override
    public List<WujinAttributeDictionaryDO> getEnabledAttributeList(String lane) {
        return attributeDictionaryMapper.selectEnabledListByLane(normalizeLane(lane), STATUS_ENABLED);
    }

    private void normalize(WujinAttributeDictionarySaveReqVO reqVO) {
        reqVO.setCode(reqVO.getCode().trim().toUpperCase());
        reqVO.setName(reqVO.getName().trim());
        reqVO.setGroupName(reqVO.getGroupName().trim());
        reqVO.setLane(normalizeLane(reqVO.getLane()));
        String valueType = reqVO.getValueType().trim().toUpperCase();
        if (!VALUE_TYPES.contains(valueType)) {
            throw new IllegalArgumentException("属性值类型无效：" + reqVO.getValueType());
        }
        reqVO.setValueType(valueType);
        List<String> options = normalizeOptions(reqVO.getValueOptions());
        if (isEnumType(valueType) && options.isEmpty()) {
            throw new IllegalArgumentException("枚举类型属性必须配置可选值");
        }
        if (VALUE_TYPE_NUMBER.equals(valueType) || VALUE_TYPE_BOOLEAN.equals(valueType)) {
            options = Collections.emptyList();
        }
        reqVO.setValueOptions(options);
        if (reqVO.getRequiredFlag() == null) {
            reqVO.setRequiredFlag(false);
        }
        if (reqVO.getSearchableFlag() == null) {
            reqVO.setSearchableFlag(false);
        }
        if (reqVO.getSort() == null) {
            reqVO.setSort(0);
        }
    }

    private String normalizeLane(String lane) {
        if (lane == null || lane.trim().isEmpty()) {
            return null;
        }
        try {
            return WujinLane.valueOf(lane.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("适用泳道无效：" + lane);
        }
    }

    private List<String> normalizeOptions(List<String> options) {
        if (options == null) {
            return Collections.emptyList();
        }
        Set<String> values = new LinkedHashSet<>();
        for (String option : options) {
            if (option != null && !option.trim().isEmpty()) {
                values.add(option.trim());
            }
        }
        return new ArrayList<>(values);
    }

    static boolean isEnumType(String valueType) {
        return VALUE_TYPE_ENUM.equals(valueType) || VALUE_TYPE_MULTI_ENUM.equals(valueType);
    }

    private void validateCodeUnique(Long id, String code) {
        WujinAttributeDictionaryDO existing = attributeDictionaryMapper.selectByCode(code);
        if (existing != null && !existing.getId().equals(id)) {
            throw new IllegalArgumentException("属性编码已存在：" + code);
        }
    }

    private void validateAttributeExists(Long id) {
        if (id == null || attributeDictionaryMapper.selectById(id) == null) {
            throw new IllegalArgumentException("平台属性字典不存在");
        }
    }
}
