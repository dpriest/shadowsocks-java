package com.dpriest.shadowsocks.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmployeeTest extends SpringTest{

    @Test
    public void testEmployee() {
        Employee employee = (Employee) getBean("employee");
        employee.setName("zhangsan");
        assertEquals("zhangsan", employee.getName());
    }

}