package com.decorator.test.toppings.veg;

import com.decorator.test.Pizza;
import com.decorator.test.PizzaDecorator;


/**
 * Created by Chaklader on 2/13/17.
 */
public class Broccoli extends PizzaDecorator {

    private final Pizza pizza;

    public Broccoli(Pizza pizza) {
        this.pizza = pizza;
    }

    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Broccoli (9.25)";
    }

    @Override
    public double getPrice() {
        return pizza.getPrice() + 9.25;
    }
}
