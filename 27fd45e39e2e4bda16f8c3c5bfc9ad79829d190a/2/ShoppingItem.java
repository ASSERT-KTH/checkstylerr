package com.balazsholczer.visitor;

public interface ShoppingItem {
	public double accept(ShoppingCartVisitor visitor);
}
