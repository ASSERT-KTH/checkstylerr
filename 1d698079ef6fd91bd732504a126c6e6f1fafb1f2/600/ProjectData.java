package com.hlj.redis.project.data;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * Created by j.sh on 28/11/2017.
 */
@Data
@Accessors(chain = true)
public class ProjectData  implements Serializable {


    private String name;
    private String Group;

    public ProjectData() {
    }

    public ProjectData(String name, String group) {
        this.name = name;
        Group = group;
    }
}
