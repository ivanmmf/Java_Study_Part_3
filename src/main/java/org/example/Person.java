package org.example;

import org.example.Annotations.Cache;
import org.example.Annotations.Mutator;

public class Person implements SalaryCalculation {
    private String name;
    private int grade;
    private int age;
    private double salary;

    public Person(String name, int grade, int age) {
        this.name = name;
        this.grade = grade;
        this.age = age;
    }
    public Person(Person person) {
        this.name = person.name;
        this.grade = person.grade;
        this.age = person.age;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Mutator
    @Override
    public void setGrade(int grade) {
        this.grade = grade;
    }

    public double getSalary() {
        return salary;
    }

    @Mutator
    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Cache(100000)
    public double salaryCalculation() {
        return this.grade * 1.75 +  age/10f;
    }


    public boolean equalsCache(Object o) {
        Person person = (Person) o;
        return grade == person.grade && age == person.age;
    }


}
