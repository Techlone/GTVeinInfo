package org.prank.config;

public class Property
{
    public enum Type
    {
        STRING,
        INTEGER,
        BOOLEAN,
        DOUBLE,
        COLOR,
        MOD_ID;

        public static Type tryParse(char id)
        {
            for (int x = 0; x < values().length; x++)
            {
                if (values()[x].getID() == id)
                {
                    return values()[x];
                }
            }

            return STRING;
        }

        public char getID()
        {
            return name().charAt(0);
        }
    }

    private String value;
    private String defaultValue;

    public Property(String value)
    {
        this.value = value;
        this.defaultValue = value;
    }

    public Property(String[] values)
    {
        this.value = "";
        this.defaultValue = "";
        for (String s : values)
            this.defaultValue += ", [" + s + "]";
        this.defaultValue = this.defaultValue.replaceFirst(", ", "");
    }

    public String getString()
    {
        return value;
    }

    /**
     * Returns the value in this property as an integer,
     * if the value is not a valid integer, it will return the initially provided default.
     *
     * @return The value
     */
    public int getInt()
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return Integer.parseInt(defaultValue);
        }
    }

    /**
     * Returns the value in this property as a boolean, if the value is not a valid boolean, it will return the provided default.
     *
     * @return The value as a boolean, or the default
     */
    public boolean getBoolean()
    {
        if (isBooleanValue())
        {
            return Boolean.parseBoolean(value);
        }
        else
        {
            return Boolean.parseBoolean(defaultValue);
        }
    }

    /**
     * Checks if the current value held by this property is a valid boolean value.
     *
     * @return True if it is a boolean value
     */
    public boolean isBooleanValue()
    {
        return ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()));
    }
}