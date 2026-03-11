package dev.magadiflo.app.service;

import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface EmployeeService {
    Observable<EmployeeResponse> getAllEmployees(String position, Boolean isFullTime);

    Single<EmployeeResponse> showEmployee(Long employeeId);

    Single<EmployeeResponse> createEmployee(EmployeeRequest request);

    Single<EmployeeResponse> updateEmployee(Long employeeId, EmployeeRequest employeeRequest);

    Completable deleteEmployee(Long employeeId);
}
