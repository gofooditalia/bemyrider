package com.app.bemyrider.model;

/**
 * Created by nct121 on 3/12/16.
 */
public class ModelForDrawer
{
    public int icon;
    public String name;

    public ModelForDrawer(int icon, String name)
    {
        this.icon = icon;
        this.name = name;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }

    public int getIcon(){
        return icon;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
