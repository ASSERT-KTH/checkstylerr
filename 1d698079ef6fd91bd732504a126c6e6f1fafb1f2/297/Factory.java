package com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.framework;

public abstract class Factory {

    public final Product create(String owner) {
        Product p = createProduct(owner);
        registerProduct(p);
        return p;
    }

    protected abstract Product createProduct(String owner);

    protected abstract void registerProduct(Product product);
}
