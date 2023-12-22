package org.example.Wrappers;

import org.example.Person;
import org.example.SalaryCalculation;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheWrapperTest {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @org.junit.jupiter.api.Test
    void wrapCacheExist() throws InterruptedException, IllegalAccessException,  NoSuchFieldException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculationWrapper = (SalaryCalculation) CacheWrapper.wrap(person);

        // Get internal cache
        CacheWrapper cacheWrapper = (CacheWrapper) Proxy.getInvocationHandler(salaryCalculationWrapper);

        // Get abstract method from SalaryCalculation interface
        Class<?>[] interfaces = person.getClass().getInterfaces();
        Assertions.assertEquals(1, interfaces.length);
        var cache = getWrapperCache(cacheWrapper);

        assertEquals(cache.size(),0);
        salaryCalculationWrapper.salaryCalculation();

        var personTest = cache.keySet().stream().filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("grade");
                field.setAccessible(true);
                return field.get(x).equals(14);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("age");
                field.setAccessible(true);
                return field.get(x).equals(37);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).findFirst().get();

        //Check that cache contains person and method

        Method[] methodTest = cache.get(personTest).keySet().toArray(new Method[0]);
        assertEquals(cache.get(personTest).size(),1);
        assertEquals(cache.size(),1);
        assertTrue(cache.containsKey(personTest));
        assertEquals(methodTest[0].getName(),"salaryCalculation");
    }

    @org.junit.jupiter.api.Test
    void wrapMultiStates() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculationWrapper = (SalaryCalculation) CacheWrapper.wrap(person);

        // Get internal cache
        CacheWrapper cacheWrapper = (CacheWrapper) Proxy.getInvocationHandler(salaryCalculationWrapper);
        salaryCalculationWrapper.salaryCalculation();
        person.setAge(25);
        salaryCalculationWrapper.salaryCalculation();

        // Get abstract method from SalaryCalculation interface
        Class<?>[] interfaces = person.getClass().getInterfaces();
        Assertions.assertEquals(1, interfaces.length);
        var cache = getWrapperCache(cacheWrapper);

        assertEquals(cache.size(),2);

        var personTest = cache.keySet().stream().filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("grade");
                field.setAccessible(true);
                return field.get(x).equals(14);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("age");
                field.setAccessible(true);
                return field.get(x).equals(37);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).findFirst().get();

        var personTest1 = cache.keySet().stream().filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("grade");
                field.setAccessible(true);
                return field.get(x).equals(14);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("age");
                field.setAccessible(true);
                return field.get(x).equals(25);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).findFirst().get();

        //Check that for different states of object 2 different records in cache were made
        assertEquals(cache.size(),2);
        assertTrue(cache.containsKey(personTest));
        assertTrue(cache.containsKey(personTest1));
    }

    @SuppressWarnings("unchecked")
    private static Map<Person, Map<Method,Object>> getWrapperCache(CacheWrapper cacheWrapper)
            throws NoSuchFieldException, IllegalAccessException {
        Field cacheField = cacheWrapper.getClass().getDeclaredField("cache");
        cacheField.setAccessible(true);
        return (Map<Person, Map<Method,Object>>) cacheField.get(cacheWrapper);
    }

    private static Map<Person, Map<Method,Object>> getWrapperLifeTimeCache(CacheWrapper cacheWrapper)
            throws NoSuchFieldException, IllegalAccessException {
        Field cacheField = cacheWrapper.getClass().getDeclaredField("cacheLifeTime");
        cacheField.setAccessible(true);
        return (Map<Person, Map<Method,Object>>) cacheField.get(cacheWrapper);
    }

    @org.junit.jupiter.api.Test
    void wrapCacheMethod() throws InterruptedException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculationWrapper = (SalaryCalculation) CacheWrapper.wrap(person);

        // Get internal cache
        CacheWrapper cacheWrapper = (CacheWrapper) Proxy.getInvocationHandler(salaryCalculationWrapper);
        var testCache = getWrapperCache(cacheWrapper);

        // Get abstract method from SalaryCalculation interface
        Class<?>[] interfaces = person.getClass().getInterfaces();
        Assertions.assertEquals(1, interfaces.length);
        var methodTest = interfaces[0].getDeclaredMethod("salaryCalculation");

        // Populate cache and overwrite value
        Assertions.assertEquals(0, testCache.size());
        salaryCalculationWrapper.salaryCalculation();
        Assertions.assertEquals(1, testCache.size());
        Object prevValue = testCache.values().iterator().next().put(methodTest, 111.00);
        Assertions.assertNotNull(prevValue);

        // Check that the new cached value is returned
        assertEquals(111.00, salaryCalculationWrapper.salaryCalculation());
    }


    @org.junit.jupiter.api.Test
    void wrapCacheClearMethod() throws InterruptedException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculationWrapper = (SalaryCalculation) CacheWrapper.wrap(person, 100);

        // Get internal cache
        CacheWrapper cacheWrapper = (CacheWrapper) Proxy.getInvocationHandler(salaryCalculationWrapper);
        var testCache = getWrapperCache(cacheWrapper);
        var testCacheLifeTime = getWrapperLifeTimeCache(cacheWrapper);

        // Get abstract method from SalaryCalculation interface
        Class<?>[] interfaces = person.getClass().getInterfaces();
        Assertions.assertEquals(1, interfaces.length);
        var methodTest = interfaces[0].getDeclaredMethod("salaryCalculation");

        // Populate cacheLifeTime and overwrite value
        Assertions.assertEquals(0, testCache.size());
        double correctValue = salaryCalculationWrapper.salaryCalculation();
        Assertions.assertEquals(1, testCache.size());
        Object prevValueCache = testCache.values().iterator().next().put(methodTest, 111.00);
        Object prevValueCacheLifeTime = testCacheLifeTime.values().iterator().next().put(methodTest, Instant.ofEpochSecond(0));
        Assertions.assertNotNull(prevValueCache);
        Assertions.assertNotNull(prevValueCacheLifeTime);
        Thread.sleep(1000);

        // Check that old time method was cleaned and method calculate without cache
        assertEquals(0, getWrapperCache(cacheWrapper).size());
        assertEquals(correctValue, salaryCalculationWrapper.salaryCalculation());
    }

    @org.junit.jupiter.api.Test
    void wrapCacheUpdateMethodDateTime() throws InterruptedException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        Person person = new Person("Ivan", 14, 37);

        SalaryCalculation salaryCalculationWrapper = (SalaryCalculation) CacheWrapper.wrap(person, 100);


        // Get internal cache
        CacheWrapper cacheWrapper = (CacheWrapper) Proxy.getInvocationHandler(salaryCalculationWrapper);
        var testCacheLifeTime = getWrapperLifeTimeCache(cacheWrapper);
        salaryCalculationWrapper.salaryCalculation();
        var personTest = testCacheLifeTime.keySet().stream().filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("grade");
                field.setAccessible(true);
                return field.get(x).equals(14);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).filter(x-> {
            try {
                Field field = x.getClass().getDeclaredField("age");
                field.setAccessible(true);
                return field.get(x).equals(37);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).findFirst().get();

        // Get abstract method from SalaryCalculation interface
        Class<?>[] interfaces = person.getClass().getInterfaces();
        Assertions.assertEquals(1, interfaces.length);
        var methodTest = interfaces[0].getDeclaredMethod("salaryCalculation");
        salaryCalculationWrapper.salaryCalculation();

        // Populate cacheLifeTime and overwrite value
        Instant methodLifeTimeFirst = (Instant) testCacheLifeTime.get(personTest).get(methodTest);
        Thread.sleep(1000);
        salaryCalculationWrapper.salaryCalculation();
        Instant methodLifeTimeSecond = (Instant) testCacheLifeTime.get(personTest).get(methodTest);

        // Check that method time is updated
        assertTrue(methodLifeTimeFirst.isBefore(methodLifeTimeSecond));
    }
}