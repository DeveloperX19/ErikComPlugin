package de.developerx19.erikcomplugin;

public class Result<T>
{
    // TODO : use in Databse.java
    public final T value;
    public final boolean data;

    public Result()
    {
        value = null;
        data = false;
    }
    public Result(T t)
    {
        value = t;
        data = true;
    }

    public T get(T defaultValue)
    {
        return data ? value : defaultValue;
    }
    public boolean test()
    {
        return data;
    }
}
