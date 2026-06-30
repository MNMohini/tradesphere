package com.bnagritech.tradesphere.employee.controller;

import com.bnagritech.tradesphere.employee.dto.EmployeeRequest;
import com.bnagritech.tradesphere.employee.dto.EmployeeResponse;
import com.bnagritech.tradesphere.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping("/create")
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request){
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeResponse>>getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
    @GetMapping("/empId/{employeeId}")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmployeeId(@PathVariable String employeeId){
        return ResponseEntity.ok(employeeService.getEmployeeByEmployeeId(employeeId));
    }
    @GetMapping("/name/{userName}")
    public ResponseEntity<EmployeeResponse> getEmployeeByUserName(@PathVariable String userName){
        return ResponseEntity.ok(employeeService.getEmployeeByUserName(userName));
    }

    @PutMapping("/empId{employeeId}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
        @PathVariable String employeeId,
        @RequestBody EmployeeRequest request)
    {
        return ResponseEntity.ok(employeeService.updateEmployee(employeeId,request));
    }
    @DeleteMapping("/empId/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable String employeeId){
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee details deleted  successfully");
    }


}
