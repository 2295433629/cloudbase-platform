package com.cloudbase.common.utils;

import java.util.Collection;

/**
 * 字符串工具类（参考 RuoYi StringUtils）
 *
 * @author ruoyi
 */
public class StringUtils extends org.springframework.util.StringUtils {

    /** 空字符串 */
    private static final String NULLSTR = "";

    /**
     * 获取参数不为空值
     */
    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * 获取参数为空值
     */
    public static boolean isNull(Object obj) {
        if (obj == null) return true;
        if (obj instanceof CharSequence) return ((CharSequence) obj).length() == 0;
        if (obj instanceof Collection) return ((Collection<?>) obj).isEmpty();
        if (obj.getClass().isArray()) return java.lang.reflect.Array.getLength(obj) == 0;
        return false;
    }

    /**
     * 获取参数不为空值
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 获取参数为空值
     */
    public static boolean isEmpty(Object obj) {
        return isNull(obj);
    }

    /**
     * 驼峰转下划线命名
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder();
        boolean preCharIsUpperCase = true;
        boolean curCharIsUpperCase;
        boolean nextCharIsUpperCase;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            curCharIsUpperCase = Character.isUpperCase(c);
            if (i < (str.length() - 1)) {
                nextCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }
            if (preCharIsUpperCase && curCharIsUpperCase && (i < str.length() - 1)) {
                sb.append(c);
            } else {
                if (i < (str.length() - 1) && Character.isLowerCase(c) && preCharIsUpperCase) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 下划线转驼峰命名
     */
    public static String toCamelCase(String s) {
        if (s == null) return null;
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '_') {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将对象转为字符串<br>
     * 1、Byte数组和ByteBuffer会被转为对应字符串
     * 2、对象数组会调用Arrays.toString
     */
    public static String utf8Str(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof byte[]) return new String((byte[]) obj);
        return obj.toString();
    }

    /**
     * 格式化文本，{} 表示占位符<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数数组<br>
     * 如果想输出 {} 使用 \\{ 和 \\}转义<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") = this is a for b<br>
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params   参数值
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params) {
        if (isEmpty(params) || isEmpty(template)) return template;
        return StrFormatter.format(template, params);
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        int strLen = (str == null) ? 0 : str.length();
        if (strLen == 0) return str;
        char firstChar = str.charAt(0);
        char newChar = Character.toTitleCase(firstChar);
        if (firstChar == newChar) return str;
        char[] newChars = new char[strLen];
        newChars[0] = newChar;
        str.getChars(1, strLen, newChars, 1);
        return String.valueOf(newChars);
    }
}
