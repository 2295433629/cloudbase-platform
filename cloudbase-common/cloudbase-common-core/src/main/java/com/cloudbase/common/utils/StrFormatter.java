package com.cloudbase.common.utils;

/**
 * 字符串格式化（{} 占位符替换）
 *
 * @author ruoyi
 */
class StrFormatter {

    /** 占位符 */
    private static final String EMPTY_JSON = "{}";
    /** 转义起始符 */
    private static final char C_BACKSLASH = '\\';

    /**
     * 格式化字符串<br>
     * 将模板中的 {} 按顺序替换为参数
     *
     * @param strPattern 字符串模板
     * @param argArray   参数列表
     * @return 结果
     */
    public static String format(final String strPattern, final Object... argArray) {
        if (strPattern == null || argArray == null || argArray.length == 0) {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();
        StringBuilder sb = new StringBuilder(strPatternLength + 50);
        int handledPosition = 0;
        int delimIndex;
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
            if (delimIndex == -1) {
                if (handledPosition == 0) {
                    return strPattern;
                }
                sb.append(strPattern, handledPosition, strPatternLength);
                return sb.toString();
            }
            if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH) {
                if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH) {
                    sb.append(strPattern, handledPosition, delimIndex - 1);
                    sb.append(StringUtils.utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                } else {
                    sb.append(strPattern, handledPosition, delimIndex - 1);
                    sb.append(EMPTY_JSON);
                    handledPosition = delimIndex + 2;
                }
            } else {
                sb.append(strPattern, handledPosition, delimIndex);
                sb.append(StringUtils.utf8Str(argArray[argIndex]));
                handledPosition = delimIndex + 2;
            }
        }
        sb.append(strPattern, handledPosition, strPatternLength);
        return sb.toString();
    }
}
