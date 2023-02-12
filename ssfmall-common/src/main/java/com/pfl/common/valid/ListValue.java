package com.pfl.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义校验注解
 *  1. 创建注解
 *      指定报错信息，根据配置文件指定
 *  2. 指定校验器，可以指定多个校验器
 */
@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
// 可以标注在那些位置
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {

    // JRS303 规范
    String message() default "{com.pfl.common.valid.ListValue.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int[] value() default {};
}
