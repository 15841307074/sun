package com.htyoudao.youdao.module.order.dal.dataobject.collection;

import org.springframework.util.ObjectUtils;

import java.io.Serial;
import java.util.HashSet;

/**
 * <p>
 * 禁止添加null元素
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-04
 */
public class NotNullHashSet<E> extends HashSet<E> {

    @Serial
    private static final long serialVersionUID = 5539273400687486637L;

    @Override

    public boolean add(E e) {

        if (ObjectUtils.isEmpty(e)) {

//            throw new IllegalArgumentException("Null elements are not allowed");

            return false;

        }

        return super.add(e);

    }
}
