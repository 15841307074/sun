package com.htyoudao.youdao.framework.common.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 终端的枚举
 *
 * @author 0090
 */
@RequiredArgsConstructor
@Getter
public enum TerminalEnum implements ArrayValuable<Integer> {

    UNKNOWN(0, "未知"), // 目的：在无法解析到 terminal 时，使用它
    WECHAT_MINI_PROGRAM(10, "微信小程序"),
    WECHAT_WAP(11, "微信公众号"),
    H5(20, "H5 网页"),
    APP(31, "手机 App"),
    WEB(51, "web 端"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(TerminalEnum::getTerminal).toArray(Integer[]::new);

    public static final Integer[] IM_ARRAYS = {APP.getTerminal(), WEB.getTerminal()};

    /**
     * 终端
     */
    private final Integer terminal;
    /**
     * 终端名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static TerminalEnum getByTerminal(Integer terminal) {
        return Arrays.stream(values())
                .filter(item -> item.terminal.equals(terminal))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static boolean contains(Integer terminal) {
        return Arrays.stream(values()).anyMatch(item -> item.terminal.equals(terminal));
    }
}
