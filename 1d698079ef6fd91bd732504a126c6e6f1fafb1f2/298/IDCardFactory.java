package com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.idcard;

import com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.framework.Factory;
import com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.framework.Product;

import java.util.*;

public class IDCardFactory extends Factory {

    private List owners = new ArrayList();

    protected Product createProduct(String owner) {
        return new IDCard(owner);
    }

    protected void registerProduct(Product product) {
        owners.add(((IDCard) product).getOwner());
    }

    public List getOwners() {
        return owners;
    }
}
