package com.bnagritech.tradesphere.employee.controller;

import com.bnagritech.tradesphere.employee.dto.EmployeeRequest;
import com.bnagritech.tradesphere.employee.dto.EmployeeResponse;
import com.bnagritech.tradesphere.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request){
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }
    /*
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<EmployeeResponse>>getAllEmployees(){
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }*/
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmployeeId(@PathVariable String employeeId){
        return ResponseEntity.ok(employeeService.getEmployeeByEmployeeId(employeeId));
    }
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
        @PathVariable String employeeId,
        @RequestBody EmployeeRequest request)
    {
        return ResponseEntity.ok(employeeService.updateEmployee(employeeId,request));
    }
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable String employeeId){
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee details deleted  successfully");
    }


}
