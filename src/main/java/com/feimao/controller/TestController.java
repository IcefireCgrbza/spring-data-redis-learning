package com.feimao.controller;

import com.alibaba.fastjson.JSON;
import com.feimao.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @PostMapping(value = "/save/{key}/{value}")
    @ResponseBody
    public String save(@PathVariable String key, @PathVariable String value) {
        transactionTemplate.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus status) {
                redisTemplate.opsForValue().set("k1", "v1");
                throw new RuntimeException();
            }
        });
        redisTemplate.opsForValue().set(key, new Person()
                .setName("feimao")
                .setAge(22)
                .setAddress(new Person.Address()
                        .setCity("shanghai")
                        .setCountry("china")));
        throw new RuntimeException();
//        return "success";
    }

    @GetMapping(value = "/find/{key}")
    @ResponseBody
    public String find(@PathVariable String key) {
        HashMapper hashMapper = new Jackson2HashMapper(true);
        Map hash = hashMapper.toHash(new Person()
                .setName("feimao")
                .setAge(22)
                .setAddress(new Person.Address()
                        .setCity("shanghai")
                        .setCountry("china")));
        return redisTemplate.boundValueOps(key).get().toString();
    }

    @GetMapping(value = "/hash/jackson2")
    @ResponseBody
    public String toHashJackson2() {
        //通过true和false决定hash转换方式
        String a = (String) null;
        HashMapper hashMapper = new Jackson2HashMapper(false);
        Map hash = hashMapper.toHash(new Person()
                .setName("feimao")
                .setAge(22)
                .setAddress(new Person.Address()
                        .setCity("shanghai")
                        .setCountry("china")));
        return JSON.toJSONString(hash);
    }

    @GetMapping(value = "/hash/object")
    @ResponseBody
    public String toHashObject() {
        HashMapper hashMapper = new ObjectHashMapper();
        Map hash = hashMapper.toHash(new Person()
                .setName("feimao")
                .setAge(22)
                .setAddress(new Person.Address()
                        .setCity("shanghai")
                        .setCountry("china")));
        return JSON.toJSONString(hash);
    }

    @GetMapping(value = "/hash/bean-utils")
    @ResponseBody
    public String toHashStringUtil() {
        //通过true和false决定hash转换方式
        HashMapper hashMapper = new BeanUtilsHashMapper(Person.class);
        Map hash = hashMapper.toHash(new Person()
                .setName("feimao")
                .setAge(22)
                .setAddress(new Person.Address()
                        .setCity("shanghai")
                        .setCountry("china")));
        return JSON.toJSONString(hash);
    }

    @GetMapping(value = "/multi-exec")
    @ResponseBody
    public String multiExec() {
        List<Object> results = (List<Object>) redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                for (int i=0;i<10000;i++) {
                    operations.boundValueOps("k" + i).set("value" + i);
                }
                return operations.exec();
            }
        });
        return JSON.toJSONString(results.get(0));
    }

}
