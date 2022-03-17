
package org.monte.media;

import java.io.Serializable;


public class FormatKey<T> implements Serializable, Comparable {

    public static final long serialVersionUID = 1L;

    private String key;

    private String name;

    private Class<T> clazz;


    private boolean comment;


    public FormatKey(String key, Class<T> clazz) {
        this(key, key, clazz);
    }


    public FormatKey(String key, String name, Class<T> clazz) {
        this(key,name,clazz,false);
    }

    public FormatKey(String key, String name, Class<T> clazz, boolean comment) {
        this.key = key;
        this.name = name;
        this.clazz = clazz;
        this.comment=comment;
    }


    public String getKey() {
        return key;
    }


    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return key;
    }


    public boolean isAssignable(Object value) {
        return clazz.isInstance(value);
    }

    public boolean isComment() {
        return comment;
    }


    public Class getValueClass() {
        return clazz;
    }

    @Override
    public int compareTo(Object o) {
        return compareTo((FormatKey) o);
    }

    public int compareTo(FormatKey that) {
        return this.key.compareTo(that.key);
    }
}
