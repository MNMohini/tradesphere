package com.bnagritech.tradesphere.employee.service.impl;

import com.bnagritech.tradesphere.common.exception.EmployeeAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.EmployeeNotFoundException;
import com.bnagritech.tradesphere.common.exception.UserAlreadyExistsException;
import com.bnagritech.tradesphere.employee.dto.EmployeeRequest;
import com.bnagritech.tradesphere.employee.dto.EmployeeResponse;
import com.bnagritech.tradesphere.employee.model.Employee;
import com.bnagritech.tradesphere.employee.repository.EmployeeRepository;
import com.bnagritech.tradesphere.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new EmployeeAlreadyExistsException("Email already exists");
        }
        if (employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new EmployeeAlreadyExistsException("Employee Id already exists");
        }
        if (employeeRepository.existsByUserName(request.getUserName())) {
            throw new UserAlreadyExistsException("User Name already exists");
        }

        Employee employee = Employee.builder()
                .employeeId(request.getEmployeeId())
                .employeeName(request.getEmployeeName())
                .email(request.getEmail())
                .userName(request.getUserName())
                .phoneNumber(Long.parseLong(request.getPhoneNumber()))
                .role(request.getRole())
                .territoryId(request.getTerritoryId())
                .joiningDate(request.getJoiningDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        return mapToResponse(savedEmployee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse getEmployeeByUserName(String userName) {
        Employee employee = employeeRepository.findByUserName(userName).orElseThrow(
                ()-> new EmployeeNotFoundException("Employee Not Found"));
        return mapToResponse(employee);
    }

    @Override
    public EmployeeResponse getEmployeeByEmployeeId(String employeeId) {

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        return mapToResponse(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(String employeeId, EmployeeRequest request) {

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        employee.setEmployeeName(request.getEmployeeName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(Long.parseLong(request.getPhoneNumber()));
        employee.setRole(request.getRole());
        employee.setTerritoryId(request.getTerritoryId());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setUpdatedAt(LocalDateTime.now());

        Employee updatedEmployee = employeeRepository.save(employee);

        return mapToResponse(updatedEmployee);
    }

    @Override
    public void deleteEmployee(String employeeId) {

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        employeeRepository.delete(employee);
    }

    private EmployeeResponse mapToResponse(Employee employee) {

        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getEmployeeName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .role(employee.getRole())
                .territoryId(employee.getTerritoryId())
                .joiningDate(employee.getJoiningDate())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
