package com.state.robot;

/**
 * Created by Chaklader on 2/15/17.
 */


/*
* State is not the same as the behaviour
*
* states preceeds the behaviour the robot will pick a 
* behaviour from a finite set while being in particular 
* state
* */
public interface RoboticState {

    // we dont need a on state as on is composed of walk and the cook state itself
    public void off();
    public void walk();
    public void cook();
}
