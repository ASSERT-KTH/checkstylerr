package com.hlj.moudle.design.D09避免浪费.D20Flyweight享元模式;

/**
 * @author HealerJean
 * @ClassName D20Main
 * @date 2019/8/21  10:27.
 * @Description
 */
public class D20Main {

    public static void main(String[] args) {
        FlyweightFactory factory = new FlyweightFactory();

        User user = new User();
        user.setName("HealerJean");
        AbstractWebSite product = factory.getWebSiteCategory("产品展示");
        product.create(user);


        AbstractWebSite boke = factory.getWebSiteCategory("博客");
        user.setName("HealerJean");
        boke.create(user);


        System.out.println("网站分类总数为：" + factory.getWebSiteCount());
    }
}
