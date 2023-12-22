package org.example;

import org.example.Annotations.Cache;

import java.lang.reflect.UndeclaredThrowableException;


public interface SalaryCalculation {

    double salaryCalculation() throws InterruptedException, UndeclaredThrowableException;
    void setAge(int age);
    void setGrade(int grade);

}
