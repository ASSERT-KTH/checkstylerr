/*
 * Copyright (c) 2015 - Present. The STARTS Team. All Rights Reserved.
 */

package inter;

import java.util.ArrayList;
import java.util.Arrays;

public class GrandChild extends Child {
    @Override
    public ArrayList<String> toStringsBaseA(){
        return new ArrayList<>(Arrays.asList("BaseA", "GrandChild"));
    }
}
