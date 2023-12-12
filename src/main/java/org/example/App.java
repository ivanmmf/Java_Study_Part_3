package org.example;

import org.example.Wrappers.CacheWrapper;

public class App
{

    public static void main( String[] args ) throws InterruptedException {
        Person person = new Person("Ivan",14,37);

        SalaryCalculation salaryCalculation = CacheWrapper.wrap(person);
        System.out.println(salaryCalculation.salaryCalculation());
        System.out.println(salaryCalculation.salaryCalculation());
        salaryCalculation.setAge(85);
        System.out.println(salaryCalculation.salaryCalculation());
        System.out.println(salaryCalculation.salaryCalculation());
        System.out.println();
    }

}
