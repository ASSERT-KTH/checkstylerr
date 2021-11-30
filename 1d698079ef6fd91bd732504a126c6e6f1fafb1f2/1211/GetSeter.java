package com.hlj.ddkj.lombok;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


public class GetSeter {

    @Getter
    @Setter
    private String name;

    @Setter(AccessLevel.PROTECTED)
    private int age;

    @Getter(AccessLevel.PUBLIC)
    private String language;



}
