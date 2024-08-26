package com.vv.vada.model.enums;

import cn.hutool.core.util.ObjectUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vv先森
 * @create 2024-08-26 21:43
 */
public enum ScoreStrategy {
    ;

    private final String text;

    private final int code;

    ScoreStrategy(String text, int code) {
        this.text = text;
        this.code = code;
    }

    public static ScoreStrategy getScoreStrategy(int code) {
        if (ObjectUtil.isEmpty(code)) {
            return null;
        }
        for (ScoreStrategy scoreStrategy : ScoreStrategy.values()) {
            if (scoreStrategy.code == code) {
                return scoreStrategy;
            }
        }
        return null;
    }

    public static List<Integer> codeList(){
        return Arrays.stream(values()).map(item->item.code).collect(Collectors.toList());
    }

    public String getText() {
        return text;
    }

    public int getCode() {
        return code;
    }
}
