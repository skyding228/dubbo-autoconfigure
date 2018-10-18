package com.skyding.dubbo.autoconfigure;

/**
 * interface information
 * <p>
 * created at 2018/10/18
 *
 * @author weichunhe
 */
public class InterfaceInfo {
    /**
     * the interface class
     */
    private Class clazz;

    /**
     * The name of Bean that is the {@code clazz} property type.
     * so you can inject the Bean from Spring.
     * <p>
     * Notice: This is only available is serviceSide.
     */
    private String ref;

    public InterfaceInfo(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
