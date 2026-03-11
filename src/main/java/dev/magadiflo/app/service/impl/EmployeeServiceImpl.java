package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Employee;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.mapper.EmployeeMapper;
import dev.magadiflo.app.repository.EmployeeRepository;
import dev.magadiflo.app.service.EmployeeService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public Observable<EmployeeResponse> getAllEmployees(String position, Boolean isFullTime) {
        if (Objects.isNull(position) && Objects.isNull(isFullTime)) {
            return this.employeeRepository.findAll()
                    .map(this.employeeMapper::toEmployeeResponse)
                    .toObservable();
        }
        if (Objects.nonNull(position) && Objects.nonNull(isFullTime)) {
            return this.employeeRepository.findByPositionAndFullTime(position, isFullTime)
                    .map(this.employeeMapper::toEmployeeResponse);
        }
        if (Objects.nonNull(position)) {
            return this.employeeRepository.findByPosition(position)
                    .map(this.employeeMapper::toEmployeeResponse);
        }
        return this.employeeRepository.findByFullTime(isFullTime)
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    public Single<EmployeeResponse> showEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Maybe.error(() -> new EmployeeNotFoundException(employeeId)))
                .toSingle()
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    public Single<EmployeeResponse> createEmployee(EmployeeRequest request) {
        return this.employeeRepository.save(Employee.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .position(request.position())
                        .fullTime(request.fullTime())
                        .build()
                )
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    public Single<EmployeeResponse> updateEmployee(Long employeeId, EmployeeRequest employeeRequest) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Maybe.error(new EmployeeNotFoundException(employeeId)))
                .toSingle()
                .map(employeeDB -> {
                    log.info("Empleado encontrado: {}", employeeDB);
                    employeeDB.setFirstName(employeeRequest.firstName());
                    employeeDB.setLastName(employeeRequest.lastName());
                    employeeDB.setPosition(employeeRequest.position());
                    employeeDB.setFullTime(employeeRequest.fullTime());
                    return employeeDB;
                })
                .flatMap(employee -> this.employeeRepository.save(employee).toObservable()
                        .doOnNext(employeeDB -> log.info("Empleado actualizado: {}", employeeDB))
                        .firstOrError()
                )
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    public Completable deleteEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Maybe.error(() -> new EmployeeNotFoundException(employeeId)))
                .toSingle()
                .flatMapCompletable(employee -> this.employeeRepository.deleteById(employeeId));
    }
}
