package com.bnagritech.tradesphere.employee.service;

import com.bnagritech.tradesphere.employee.dto.EmployeeRequest;
import com.bnagritech.tradesphere.employee.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeRequest request);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeByUserName(String userName);

    EmployeeResponse getEmployeeByEmployeeId(String employeeId);
    EmployeeResponse getEmployeeByEmail(String email);
    EmployeeResponse updateEmployee(String employeeId, EmployeeRequest request);

    void  deleteEmployee(String employeeId);

}
