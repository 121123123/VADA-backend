package com.vv.vada.model.enums;

import cn.hutool.core.util.ObjectUtil;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vv先森
 * @create 2024-08-26 21:25
 */
public enum AppTypeEnum {
    SCORE("得分类", 0),

    PASS("通过类", 1);


    private final  String text;

    private final int code;

    AppTypeEnum(String text, int code) {
        this.text = text;
        this.code = code;
    }

    public static AppTypeEnum getAppTypeEnumByCode(int code) {
        if (ObjectUtil.isEmpty(code)) {
            return null;
        }
        for (AppTypeEnum anEnum : AppTypeEnum.values()) {
            if (anEnum.code == code) {
                return anEnum;
            }
        }
        return null;
    }

    public static List<Integer> getAppTypeCodeList() {
        return Arrays.stream(values()).map(item -> item.code).collect(Collectors.toList());
    }

    public String getText() {
        return text;
    }

    public int getCode() {
        return code;
    }
}
