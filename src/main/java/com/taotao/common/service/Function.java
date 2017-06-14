package com.taotao.common.service;

/**
 * Created by JARVIS on 2017/6/14.
 */
public interface Function<T,E> {
    T callback(E e);
}
