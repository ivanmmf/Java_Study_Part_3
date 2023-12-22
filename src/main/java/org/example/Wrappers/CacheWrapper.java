package org.example.Wrappers;

import org.example.Annotations.Cache;
import org.example.Person;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CacheWrapper implements InvocationHandler {
    private final Person object;
    private final Map<Method, Object> pair = new ConcurrentHashMap<>();
    private final Map<Person, Map<Method, Instant>> cacheLifeTime = new ConcurrentHashMap<>();
    private final Map<Person, Map<Method, Object>> cache = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(16);

    public Map<Object, Map<Method, Object>> getCache() {
        return Map.copyOf(cache);
    }

    public void addTime(Person p, Method m) {
        Person person = new Person(p);
        cacheLifeTime.put(person, new ConcurrentHashMap<>());
        cacheLifeTime.get(person).put(m, Instant.now());
    }

    public void resetTime(Person p, Method m) {
        Person pers = cacheLifeTime.keySet().stream().filter(x -> x.equalsCache(p)).findFirst().orElseThrow();
        cacheLifeTime.get(pers).put(m, Instant.now());
    }

    public void clearCache() {
        Instant now = Instant.now();
        for (Person p : cache.keySet()) {
            for (Person l : cacheLifeTime.keySet()) {
                if (p.equalsCache(l)) {
                    for (Method m : cache.get(p).keySet()) {
                        final Method trueMethod;
                        try {
                            trueMethod = this.object.getClass().getDeclaredMethod(m.getName(),m.getParameterTypes());
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                        Cache cacheAnnotation = trueMethod.getAnnotation(Cache.class);
                        if (cacheAnnotation == null) {
                            continue;
                        }
                        for (Method k : cacheLifeTime.get(l).keySet()) {
                            if (k.equals(m)) {
                                long lifeTimeMillis = cacheAnnotation.value();
                                Duration lifetime = Duration.ofMillis(lifeTimeMillis);
                                Instant clearBefore = now.minus(lifetime);
                                Instant timestamp = cacheLifeTime.get(l).get(k);
                                if (timestamp.isBefore(clearBefore)) {
                                    if (cache.get(p).size() > 1) {
                                        cache.get(p).remove(m);
                                    } else {
                                        cache.remove(p);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static Object wrap(Person object) {
        return wrap(object, 60000);
    }

    public static Object wrap(Person object, long cleanupIntervalMillis) {
        CacheWrapper wrapper = new CacheWrapper(object, cleanupIntervalMillis);
        return  Proxy.newProxyInstance(object.getClass().getClassLoader(), object.getClass().getInterfaces(), wrapper);
    }

    private CacheWrapper(Person object, long cleanupIntervalMillis) {
        this.object = object;
        EXECUTOR.scheduleWithFixedDelay(this::clearCache, 0, cleanupIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method declaredMethod = object.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (declaredMethod.isAnnotationPresent(Cache.class)) {
            for (Person object : cache.keySet()) {
                if (this.object.equalsCache(object)) {
                    if (cache.get(object).containsKey(method)) {
                        resetTime(object, method);
                        return cache.get(object).get(method);
                    }
                }
            }

            Person cachePerson = new Person(this.object);
            pair.put(method, method.invoke(cachePerson, args));
            Map<Method, Object> cachePair = new ConcurrentHashMap<>(pair);
            cache.put(cachePerson, cachePair);
            addTime(object, method);
            return cache.get(cachePerson).get(method);
        }
        return method.invoke(this.object, args);
    }
}
