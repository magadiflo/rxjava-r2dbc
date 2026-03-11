package dev.magadiflo.app.controller;

import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.service.EmployeeService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Single<ResponseEntity<List<EmployeeResponse>>> findAllEmployees(@RequestParam(required = false) String position,
                                                                           @RequestParam(required = false) Boolean fullTime) {
        return this.employeeService.getAllEmployees(position, fullTime)
                .doOnNext(employeeResponse -> log.info("{}", employeeResponse))
                .toList()
                .subscribeOn(Schedulers.io())
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/{employeeId}")
    public Single<ResponseEntity<EmployeeResponse>> findEmployee(@PathVariable Long employeeId) {
        return this.employeeService.showEmployee(employeeId)
                .doOnSuccess(employeeResponse -> log.info("{}", employeeResponse))
                .subscribeOn(Schedulers.io())
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Single<ResponseEntity<EmployeeResponse>> saveEmployee(@Valid @RequestBody EmployeeRequest request) {
        return this.employeeService.createEmployee(request)
                .doOnSuccess(employeeResponse -> log.info("{}", employeeResponse))
                .subscribeOn(Schedulers.io())
                .map(employeeResponse -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(employeeResponse));
    }

    @PutMapping(path = "/{employeeId}")
    public Single<ResponseEntity<EmployeeResponse>> updateEmployee(@PathVariable Long employeeId,
                                                                   @Valid @RequestBody EmployeeRequest request) {
        return this.employeeService.updateEmployee(employeeId, request)
                .doOnSuccess(employeeResponse -> log.info("{}", employeeResponse))
                .subscribeOn(Schedulers.io())
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{employeeId}")
    public Completable deleteEmployee(@PathVariable Long employeeId) {
        return this.employeeService.deleteEmployee(employeeId)
                .subscribeOn(Schedulers.io());
    }
}
