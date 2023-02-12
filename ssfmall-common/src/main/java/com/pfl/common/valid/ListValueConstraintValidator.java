package com.pfl.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = new HashSet<>();
    // 初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.value();
        for (int value : values) {
            set.add(value);
        }
    }

    // 判断是否校验成功

    /**
     *
     * @param integer 需要校验的值
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }
}