package dev.magadiflo.app.repository;

import dev.magadiflo.app.entity.Employee;
import io.reactivex.rxjava3.core.Observable;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;

public interface EmployeeRepository extends RxJava3CrudRepository<Employee, Long> {
    Observable<Employee> findByPosition(String position);

    Observable<Employee> findByFullTime(Boolean isFullTime);

    Observable<Employee> findByPositionAndFullTime(String position, Boolean isFullTime);

    Observable<Employee> findByFirstName(String firstName);
}
