package com.oneness.dine.payroll.repository;

import com.oneness.dine.payroll.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {
    List<Employee> findByBranchIdAndIsActiveTrueOrderByFullNameAsc(Long branchId);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByPunchMachineId(String punchMachineId);
    boolean existsByEmployeeCode(String employeeCode);
}
