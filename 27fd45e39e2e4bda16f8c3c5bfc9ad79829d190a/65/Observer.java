package com.balazsholczer.observer;

public interface Observer {
	public void update(int pressure, int temperature, int humidity);
}
