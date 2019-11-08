package com.cehome.easymybatis.core;

import com.cehome.easymybatis.Generation;

import java.util.HashMap;
import java.util.Map;

/**
 * coolma 2019/11/7
 **/
public class Generators {

    private static Generators instance=new Generators();
    private Map<String, Generation> map=new HashMap();
    private Generation primary;
    public static Generators getInstance(){
        return instance;
    }

    public void put(String name , Generation generation){
        map.put(name, generation);
    }
    public void putAll( Map<String, Generation> generatorMap){
        map.putAll( generatorMap);
    }

    public Generation get(String name){
        return map.get(name);
    }

    public Generation getPrimary() {
        return primary;
    }

    public void setPrimary(Generation primary) {
        this.primary = primary;
    }
}
