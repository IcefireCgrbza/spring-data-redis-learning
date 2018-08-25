package com.feimao.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Person {

    private String name;

    private Integer age;

    private Address address;

    @Data
    @Accessors(chain = true)
    public static class Address {

        private String city;

        private String country;

    }

}
