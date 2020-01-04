package com.psawesome.basepackage.learningreactivefile.repo;

import com.psawesome.basepackage.learningreactivefile.dto.Employee;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * package: com.psawesome.basepackage.learningreactivefile.repo
 * author: PS
 * DATE: 2020-01-04 토요일 20:20
 */
public interface EmployeeRepository extends ReactiveCrudRepository<Employee, String>, ReactiveQueryByExampleExecutor<Employee> {

}
