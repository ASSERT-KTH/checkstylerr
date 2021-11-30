package com.hlj.moudle.design.D09避免浪费.D20Flyweight享元模式;

import java.util.HashMap;

/**
 * @author HealerJean
 * @ClassName FlyweightFactory
 * @date 2019/8/21  10:24.
 * @Description
 */
public class FlyweightFactory {

    private HashMap<String, ConcreteWebSite> pool = new HashMap<>();

    /**
     * 获得网站分类
     */
    public AbstractWebSite getWebSiteCategory(String key) {
        if (!pool.containsKey(key)) {
            pool.put(key, new ConcreteWebSite(key));
        }
        return pool.get(key);
    }

    /**
     * 获得网站分类总数
     */
    public int getWebSiteCount() {
        return pool.size();
    }
}
