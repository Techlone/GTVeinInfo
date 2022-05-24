package org.prank.config;

import java.util.*;

public class ConfigCategory
{
    private String name;
    public Set<ConfigCategory> childs = new HashSet<>();
    public Map<String, Property> properties = new TreeMap<>();
    public final ConfigCategory parent;
    private List<String> propertyOrder = null;

    public ConfigCategory(String name)
    {
        this(name, null);
    }

    public ConfigCategory(String name, ConfigCategory parent)
    {
        this.name = name;
        this.parent = parent;
        if (parent != null)
        {
            parent.childs.add(this);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ConfigCategory)
        {
            ConfigCategory cat = (ConfigCategory)obj;
            return name.equals(cat.name) && childs.equals(cat.childs);
        }

        return false;
    }

    public String getQualifiedName()
    {
        return getQualifiedName(name, parent);
    }

    public static String getQualifiedName(String name, ConfigCategory parent)
    {
        return (parent == null ? name : parent.getQualifiedName() + Configuration.CATEGORY_SPLITTER + name);
    }

    public Property put(String key, Property value)
    {
        if (this.propertyOrder != null && !this.propertyOrder.contains(key))
            this.propertyOrder.add(key);
        return properties.put(key, value);
    }

    public String getName() {
        return name;
    }

    public Set<ConfigCategory> getChilds() {
        return childs;
    }
}