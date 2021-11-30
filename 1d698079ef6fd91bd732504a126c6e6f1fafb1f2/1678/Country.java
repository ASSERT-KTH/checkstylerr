package com.hlj.mybatisxml.entity.baseset;

import javax.persistence.*;

public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String countryname;

    private String countrycode;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return countryname
     */
    public String getCountryname() {
        return countryname;
    }

    /**
     * @param countryname
     */
    public void setCountryname(String countryname) {
        this.countryname = countryname == null ? null : countryname.trim();
    }

    /**
     * @return countrycode
     */
    public String getCountrycode() {
        return countrycode;
    }

    /**
     * @param countrycode
     */
    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode == null ? null : countrycode.trim();
    }
}