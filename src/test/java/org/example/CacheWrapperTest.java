package org.example;

import org.example.Wrappers.CacheWrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class CacheWrapperTest {


    @org.junit.jupiter.api.Test
    void wrapCacheExist() throws InterruptedException, InvocationTargetException, IllegalAccessException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculation = CacheWrapper.wrap(person);

        var method = Arrays.stream(salaryCalculation.getClass().getMethods())
                .filter(it -> it.getName().contains("getInvocationHandler")).findFirst().get();
        var wrapper = (CacheWrapper) method.invoke(salaryCalculation, salaryCalculation);
        salaryCalculation.salaryCalculation();
        assertTrue(wrapper.getCache().containsKey("salaryCalculation"));

    }

    @org.junit.jupiter.api.Test
    void wrapCacheClear() throws InterruptedException, InvocationTargetException, IllegalAccessException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculation = CacheWrapper.wrap(person);

        var method = Arrays.stream(salaryCalculation.getClass().getMethods())
                .filter(it -> it.getName().contains("getInvocationHandler")).findFirst().get();
        var wrapper = (CacheWrapper) method.invoke(salaryCalculation, salaryCalculation);
        salaryCalculation.salaryCalculation();
        salaryCalculation.setAge(85);
        assertFalse(wrapper.getCache().containsKey("salaryCalculation"));

    }

    @org.junit.jupiter.api.Test
    void wrapCacheMethod() throws InterruptedException, InvocationTargetException, IllegalAccessException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculation = CacheWrapper.wrap(person);

        var method = Arrays.stream(salaryCalculation.getClass().getMethods())
                .filter(it -> it.getName().contains("getInvocationHandler")).findFirst().get();
        var wrapper = (CacheWrapper) method.invoke(salaryCalculation, salaryCalculation);
        var cache = Arrays.stream(wrapper.getClass().getDeclaredFields()).filter(it -> it.getName().contains("cache")).findFirst().get();
        cache.setAccessible(true);
        HashMap<String, Object> testCache = new HashMap<>();
        testCache.put("salaryCalculation", 555.00);
        cache.set(wrapper, testCache);
        assertEquals(555.00, salaryCalculation.salaryCalculation());

    }

    @org.junit.jupiter.api.Test
    void wrapCacheAnotherMethod() throws InterruptedException, InvocationTargetException, IllegalAccessException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculation = CacheWrapper.wrap(person);

        var method = Arrays.stream(salaryCalculation.getClass().getMethods())
                .filter(it -> it.getName().contains("getInvocationHandler")).findFirst().get();
        var wrapper = (CacheWrapper) method.invoke(salaryCalculation, salaryCalculation);
        var cache = Arrays.stream(wrapper.getClass().getDeclaredFields()).filter(it -> it.getName().contains("cache")).findFirst().get();
        cache.setAccessible(true);
        HashMap<String, Object> testCache = new HashMap<>();
        testCache.put("not appropriate", 555.00);
        cache.set(wrapper, testCache);
        assertEquals(29, Math.ceil(salaryCalculation.salaryCalculation()));

    }

    @org.junit.jupiter.api.Test
    void wrapEmptyCache() throws InterruptedException, InvocationTargetException, IllegalAccessException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculation = CacheWrapper.wrap(person);

        var method = Arrays.stream(salaryCalculation.getClass().getMethods())
                .filter(it -> it.getName().contains("getInvocationHandler")).findFirst().get();
        var wrapper = (CacheWrapper) method.invoke(salaryCalculation, salaryCalculation);
        assertEquals(0, wrapper.getCache().size());
        assertEquals(29, Math.ceil(salaryCalculation.salaryCalculation()));

    }

}