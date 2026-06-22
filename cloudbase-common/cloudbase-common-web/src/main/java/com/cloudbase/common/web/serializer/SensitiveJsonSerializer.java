package com.cloudbase.common.web.serializer;

import com.cloudbase.common.core.annotation.Sensitive;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;

/**
 * 敏感字段脱敏 Jackson 序列化器
 * <p>
 * 配合 {@link Sensitive} 注解使用，在 JSON 输出时自动对标注了
 * {@code @Sensitive} 的 String 字段进行脱敏处理。
 * </p>
 *
 * <pre>
 * // 在实体或 VO 字段上使用：
 * &#064;Sensitive(strategy = Sensitive.SensitiveStrategy.PHONE)
 * private String phone;
 * </pre>
 */
public class SensitiveJsonSerializer extends StdScalarSerializer<Object> implements ContextualSerializer {

    private Sensitive.SensitiveStrategy strategy;

    /** Jackson 反射用的无参构造 */
    public SensitiveJsonSerializer() {
        super(Object.class);
    }

    /** 内部构造：绑定具体脱敏策略 */
    public SensitiveJsonSerializer(Sensitive.SensitiveStrategy strategy) {
        super(Object.class);
        this.strategy = strategy;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        String str = value.toString();
        gen.writeString(strategy != null ? strategy.mask(str) : str);
    }

    /**
     * Jackson 序列化上下文初始化：从字段上读取 @Sensitive 注解，绑定脱敏策略
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(Sensitive.class);
            }
            if (annotation != null) {
                return new SensitiveJsonSerializer(annotation.strategy());
            }
        }
        return this;
    }
}
