
package org.monte.media;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Format {


    private HashMap<FormatKey, Object> properties;


    public Format(Map<FormatKey, Object> properties) {
        this(properties, true);
    }


    private Format(Map<FormatKey, Object> properties, boolean copy) {
        if (copy || ! (properties instanceof HashMap)) {
            for (Map.Entry<FormatKey, Object> e : properties.entrySet()) {
                if (!e.getKey().isAssignable(e.getValue())) {
                    throw new ClassCastException(e.getValue() + " must be of type " + e.getKey().getValueClass());
                }
            }
            this.properties = new HashMap< FormatKey, Object>(properties);
        } else {
            this.properties = (HashMap< FormatKey, Object>) properties;
        }
    }


    public Format(Object... p) {
        this.properties = new HashMap< FormatKey, Object>();
        for (int i = 0; i < p.length; i += 2) {
            FormatKey key = (FormatKey) p[i];
            if (!key.isAssignable(p[i + 1])) {
                throw new ClassCastException(key + ": " + p[i + 1] + " must be of type " + key.getValueClass());
            }
            this.properties.put(key, p[i + 1]);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(FormatKey<T> key) {
        return (T) properties.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(FormatKey<T> key, T defaultValue) {
        return (properties.containsKey(key)) ? (T) properties.get(key) : defaultValue;
    }

    public boolean containsKey(FormatKey key) {
        return properties.containsKey(key);
    }


    public Map<FormatKey, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }


    public Set<FormatKey> getKeys() {
        return Collections.unmodifiableSet(properties.keySet());
    }


    public boolean matches(Format that) {
        for (Map.Entry<FormatKey, Object> e : properties.entrySet()) {
            if (!e.getKey().isComment()) {
                if (that.properties.containsKey(e.getKey())) {
                    Object a = e.getValue();
                    Object b = that.properties.get(e.getKey());
                    if (a != b && a == null || !a.equals(b)) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    public boolean matchesWithout(Format that, FormatKey... without) {
        OuterLoop:
        for (Map.Entry<FormatKey, Object> e : properties.entrySet()) {
            FormatKey k = e.getKey();
            if (!e.getKey().isComment()) {
                if (that.properties.containsKey(k)) {
                    for (int i = 0; i < without.length; i++) {
                        if (without[i] == k) {
                            continue OuterLoop;
                        }
                    }
                    Object a = e.getValue();
                    Object b = that.properties.get(k);
                    if (a != b && a == null || !a.equals(b)) {
                        return false;
                    }

                }
            }
        }
        return true;
    }


    public Format append(Format that) {
        HashMap<FormatKey, Object> m = new HashMap<FormatKey, Object>(this.properties);
        for (Map.Entry<FormatKey, Object> e : that.properties.entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        return new Format(m,false);
    }


    public Format append(Object... p) {
        HashMap<FormatKey, Object> m = new HashMap<FormatKey, Object>(this.properties);
        for (int i = 0; i < p.length; i += 2) {
            FormatKey key = (FormatKey) p[i];
            if (!key.isAssignable(p[i + 1])) {
                throw new ClassCastException(key + ": " + p[i + 1] + " must be of type " + key.getValueClass());
            }
            m.put(key, p[i + 1]);
        }
        return new Format(m,false);
    }


    public Format prepend(Format that) {
        HashMap<FormatKey, Object> m = new HashMap<FormatKey, Object>(that.properties);
        for (Map.Entry<FormatKey, Object> e : this.properties.entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        return new Format(m,false);
    }


    public Format prepend(Object... p) {
        HashMap<FormatKey, Object> m = new HashMap<FormatKey, Object>();
        for (int i = 0; i < p.length; i += 2) {
            FormatKey key = (FormatKey) p[i];
            if (!key.isAssignable(p[i + 1])) {
                throw new ClassCastException(key + ": " + p[i + 1] + " must be of type " + key.getValueClass());
            }
            m.put(key, p[i + 1]);
        }
        for (Map.Entry<FormatKey, Object> e : this.properties.entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        return new Format(m,false);
    }

    public Format intersectKeys(FormatKey... keys) {
        HashMap<FormatKey, Object> m = new HashMap<FormatKey, Object>();
        for (FormatKey k : keys) {
            if (properties.containsKey(k)) {
                m.put(k, properties.get(k));
            }
        }
        return new Format(m,false);
    }


    public Format removeKeys(FormatKey... keys) {
        boolean needsRemoval = false;
        for (FormatKey k : keys) {
            if (properties.containsKey(k)) {
                needsRemoval = true;
                break;
            }
        }
        if (!needsRemoval) {
            return this;
        }

        HashMap<FormatKey, Object> m = new HashMap<FormatKey, Object>(properties);
        for (FormatKey k : keys) {
            m.remove(k);
        }
        return new Format(m,false);
    }


    public Format containsKeys(FormatKey... keys) {
        HashMap<FormatKey, Object> m = new HashMap<FormatKey, Object>(properties);
        for (FormatKey k : keys) {
            m.remove(k);
        }
        return new Format(m,false);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Format{");
        boolean isFirst = true;
        for (Map.Entry<FormatKey, Object> e : properties.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(',');
            }
            buf.append(e.getKey().toString());
            buf.append(':');
            appendStuffedString(e.getValue(), buf);
        }
        buf.append('}');
        return buf.toString();
    }


    private static void appendStuffedString(Object value, StringBuilder stuffed) {
        if (value == null) {
            stuffed.append("null");
        }
        value = value.toString();
        if (value instanceof String) {
            for (char ch : ((String) value).toCharArray()) {
                if (ch >= ' ') {
                    stuffed.append(ch);
                } else {
                    String hex = Integer.toHexString(ch);
                    stuffed.append("\\u");
                    for (int i = hex.length(); i < 4; i++) {
                        stuffed.append('0');
                    }
                    stuffed.append(hex);
                }
            }
        }
    }
}
