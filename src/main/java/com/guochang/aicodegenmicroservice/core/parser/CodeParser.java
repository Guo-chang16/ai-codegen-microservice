package com.guochang.aicodegenmicroservice.core.parser;

public interface CodeParser<T> {

    /**
     * 解析代码
     * @param code
     * @return
     */
    T parseCode(String code);



}
