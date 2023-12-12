package org.example.Wrappers;

import org.example.Annotations.Mutator;
import org.example.Annotations.Cache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

public class CacheWrapper implements InvocationHandler {
    Object object;
    private HashMap<String, Object> cache;

    public HashMap<String, Object> getCache() {
        return new HashMap<>(cache);
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T object) {
        return (T) Proxy.newProxyInstance(object.getClass().getClassLoader(), object.getClass().getInterfaces(), new CacheWrapper(object));
    }

    public CacheWrapper(Object object) {
        this.object = object;
        this.cache = new HashMap<>();
    }


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method declaredMethod = object.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (declaredMethod.isAnnotationPresent(Mutator.class)) {
            cache.clear();
        } else if (declaredMethod.isAnnotationPresent(Cache.class)) {
            String name = method.getName();
            if (cache.containsKey(name)) {
                return cache.get(name);
            } else {
                Object value = method.invoke(object,args);
                cache.put(name, value);
                return value;
            }
        }

        return method.invoke(object,args);
    }
}