# 🔗 Spring Boot | RxJava | R2DBC

### Contexto

Este proyecto es la adaptación del proyecto
[webflux-r2dbc-joins-test](https://github.com/magadiflo/webflux-r2dbc-joins-test.git) donde trabajamos con
`webFlux`. Sin embargo, en este proyecto `rxjava-r2dbc` vamos a adaptar parte del proyecto anterior pero al estilo de
`rxJava` como reemplazo de `Reactor (Mono, Flux)`. Esto lo hacemos porque en muchas ofertas laborales piden `rxJava`,
aunque `Reactor` es la opción moderna y recomendada por el equipo de `Spring`, crearé este proyecto para ver cómo
se trabaja con `rxJava`.

---

## 📦 PASO 1: `pom.xml` — Explicación completa

Creamos el proyecto desde
[Spring Initializr](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.5.11&packaging=jar&configurationFileFormat=yaml&jvmVersion=21&groupId=dev.magadiflo&artifactId=rxjava-r2dbc&name=rxjava-r2dbc&description=Demo%20project%20for%20Spring%20Boot&packageName=dev.magadiflo.app&dependencies=web,data-r2dbc,validation,postgresql,lombok)
con las siguientes dependencias:

````xml
<!--Spring Boot 3.5.11-->
<!--Java 21-->
<dependencies>
    <!-- 1️⃣ Spring MVC clásico (sin WebFlux, sin Reactor) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 2️⃣ R2DBC (trae RxJava3CrudRepository de forma nativa) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>

    <!-- 3️⃣ Validación -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- 4️⃣ RxJava 3 (el núcleo, reemplaza a Reactor) -->
    <dependency>
        <groupId>io.reactivex.rxjava3</groupId>
        <artifactId>rxjava</artifactId>
        <version>3.1.12</version>
    </dependency>

    <!-- 5️⃣ PostgreSQL drivers -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- 6️⃣ Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- 7️⃣ Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

### 📖 Explicación de cada cambio

#### 1️⃣ `spring-boot-starter-web`

- ¿Qué hace? Trae `Spring MVC` clásico con `Tomcat` como servidor embebido. Es el starter tradicional para construir
  APIs REST con `@RestController`, `@GetMapping`, etc.
- ¿Por qué lo usamos en lugar de `spring-boot-starter-webflux`? Porque `WebFlux` trae `Reactor (Mono/Flux)` como parte
  de su núcleo, y nosotros queremos un proyecto 100% `RxJava`. `Spring MVC` no tiene ninguna dependencia con `Reactor`,
  así que es nuestra base perfecta.

#### 2️⃣ `spring-boot-starter-data-r2dbc`

- ¿Qué hace? Trae `Spring Data R2DBC`, que es el módulo de Spring para acceder a bases de datos de forma no
  bloqueante (reactiva). Lo importante para nosotros es que dentro de este starter viene la
  interfaz `RxJava3CrudRepository`, que es el repositorio nativo de `RxJava` — sin necesidad
  de puentes ni conversiones.
- ¿Por qué no usamos `spring-boot-starter-data-jpa`? JPA usa JDBC por debajo, que es bloqueante. Eso significa que cada
  consulta a la BD bloquea el hilo que la ejecuta. `R2DBC` es no bloqueante, lo cual es consistente con la filosofía
  reactiva de `RxJava`.

#### 3️⃣ `spring-boot-starter-validation`

- ¿Qué hace? Trae `Hibernate Validator`, que es la implementación de `Bean Validation`. Nos permite usar anotaciones
  como `@NotBlank`, `@NotNull`, `@Valid` en nuestros `DTOs` y controllers para validar datos de entrada.
- ¿Cambia algo respecto al proyecto original? Absolutamente nada. Esta dependencia no tiene ninguna relación con
  `Reactor` ni `RxJava`.

#### 4️⃣ `rxjava` — ⭐ La estrella del proyecto

- ¿Por qué la versión `3.1.12`? Porque usamos `RxJava 3.x`, que es la versión más moderna y compatible con `Java 21`
  y `Spring Boot 3.x`. Existe `RxJava 1.x` y `2.x` pero son versiones antiguas.
- ¿Qué hace? Es la librería principal de `RxJava 3`. Trae todos los tipos reactivos que usaremos en el proyecto.
  Aquí está la tabla de equivalencias que debes memorizar:

| RxJava 3        | Reactor      | Uso                                                                   |
|-----------------|--------------|-----------------------------------------------------------------------|
| `Single<T>`     | `Mono<T>`    | Emite exactamente **1 elemento** o 1 error.                           |
| `Maybe<T>`      | `Mono<T>`    | Puede emitir **0 o 1 elemento**.                                      |
| `Observable<T>` | `Flux<T>`    | Flujo de **múltiples elementos** `sin manejo nativo de backpressure`. |
| `Flowable<T>`   | `Flux<T>`    | Flujo de **múltiples elementos** `con soporte de backpressure`.       |
| `Completable`   | `Mono<Void>` | No emite datos, solo **señala finalización o error**.                 |

> 💡 `Backpressure` significa controlar la velocidad a la que el productor emite datos para no abrumar al consumidor.
> En un CRUD simple como el nuestro no lo necesitamos, por eso usaremos `Observable` en lugar de `Flowable`.

#### 5️⃣ Drivers de PostgreSQL

- `postgresql` — Es el driver JDBC clásico de PostgreSQL. Aunque no usamos JPA, Spring Boot lo necesita internamente
  para algunas funciones de autoconfiguración como el `ConnectionFactoryInitializer` que ejecuta los scripts
  `schema.sql` y `data.sql`.
- `r2dbc-postgresql` — Es el driver `R2DBC de PostgreSQL`. Este es el que realmente usa `Spring Data R2DBC`
  para conectarse a la base de datos de forma no bloqueante.
- ¿Por qué `scope runtime`? Porque estos drivers solo se necesitan en tiempo de ejecución, no para compilar el código.
  Tu código no importa clases de estos drivers directamente.

#### 6️⃣ lombok

- ¿Qué hace? Genera automáticamente en tiempo de compilación código repetitivo como getters, setters, constructores,
  toString, builder, etc. mediante anotaciones como @Getter, @Builder, @RequiredArgsConstructor.
- ¿Cambia algo? Nada. Es independiente de cualquier framework reactivo.

#### 7️⃣ spring-boot-starter-test

- ¿Qué hace? Trae todo lo necesario para escribir tests: JUnit 5, Mockito, AssertJ, etc.

## 📊 Resumen visual de lo que construimos

```
┌─────────────────────────────────────────────┐
│           NUESTRO STACK FINAL               │
├─────────────────┬───────────────────────────┤
│ Capa Web        │ Spring MVC (Tomcat)       │
│ Reactividad     │ RxJava 3                  │
│ Base de datos   │ R2DBC (no bloqueante)     │
│ BD Engine       │ PostgreSQL                │
│ Repositorio     │ RxJava3CrudRepository     │
│ Validación      │ Hibernate Validator       │
│ Boilerplate     │ Lombok                    │
└─────────────────┴───────────────────────────┘
```

## ⚙️ PASO 2: `application.yml` — Configuración de la aplicación

### 🔄 Versión nueva (Spring MVC + RxJava)

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: rxjava-r2dbc
  r2dbc:
    url: r2dbc:postgresql://localhost:5435/db_rxjava_r2dbc
    username: magadiflo
    password: magadiflo

logging:
  level:
    dev.magadiflo.app: debug
    io.r2dbc.postgresql.QUERY: debug
    io.r2dbc.postgresql.PARAM: debug
````

- `spring.r2dbc`
    - Esta sección le dice a Spring cómo conectarse a PostgreSQL mediante `R2DBC`. Lo único que cambiamos es el
      nombre de la base de datos `db_rxjava_r2dbc` para que sea coherente con nuestro nuevo proyecto.
    - Nota algo importante en la URL: empieza con `r2dbc:postgresql://` y no con `jdbc:postgresql://`. Esa diferencia
      es clave:

| Protocolo             | Tipo           | ¿Bloquea el hilo? |
|-----------------------|----------------|-------------------|
| `jdbc:postgresql://`  | JDBC clásico   | ✅ Sí bloquea      |
| `r2dbc:postgresql://` | R2DBC reactivo | ❌ No bloquea      |

Nosotros usamos `r2dbc` porque queremos que el acceso a la base de datos sea no bloqueante,
coherente con la filosofía reactiva de `RxJava`.

- `logging`. Estas tres líneas son de logging y no tienen relación con Reactor ni RxJava:
    - `dev.magadiflo.app: debug` — Muestra todos los logs de nivel DEBUG de tu propio código.
    - `io.r2dbc.postgresql.QUERY: debug` — Muestra las queries SQL que `R2DBC` ejecuta en `PostgreSQL`. Muy útil para
      depurar.
    - `io.r2dbc.postgresql.PARAM: debug` — Muestra los parámetros que se pasan a esas queries. Útil para ver los valores
      reales en cada consulta.

> 💡 El `application.yml` prácticamente no cambia porque `R2DBC` sigue siendo nuestra forma de conectarnos a la
> base de datos. Lo que cambia en este proyecto no es la configuración, sino la forma en que consumimos esa conexión en
> el código Java.

## 🗄️ PASO 3: Scripts SQL — `schema.sql` y `data.sql`

### 🔍 ¿Cambia algo respecto al proyecto original?

Absolutamente nada. Los scripts SQL son independientes de cualquier framework reactivo. Son SQL puro y se quedan
exactamente igual.

### 📁 Ubicación

Crea la carpeta `src/main/resources/sql/` y dentro los dos archivos:

#### `schema.sql` — igual que el original

Crea la tabla employees si no existe. BIGSERIAL es un tipo de PostgreSQL que genera automáticamente un número entero
largo autoincremental, perfecto para IDs.

````
CREATE TABLE IF NOT EXISTS employees(
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    is_full_time BOOLEAN NOT NULL
);
````

#### `data.sql` — igual que el original

Primero limpia la tabla con `TRUNCATE` y reinicia el contador del ID, luego inserta 20 empleados de prueba. Esto
garantiza que cada vez que la aplicación arranque, la base de datos tenga siempre los mismos datos limpios.

````
TRUNCATE TABLE employees RESTART IDENTITY CASCADE;

INSERT INTO employees(first_name, last_name, position, is_full_time)
VALUES ('Carlos', 'Gómez', 'Gerente', true),
       ('Ana', 'Martínez', 'Desarrollador', true),
       ('Luis', 'Fernández', 'Diseñador', false),
       ('María', 'Rodríguez', 'Analista', true),
       ('José', 'Pérez', 'Soporte', true),
       ('Laura', 'Sánchez', 'Desarrollador', true),
       ('Jorge', 'López', 'Analista', false),
       ('Sofía', 'Díaz', 'Gerente', true),
       ('Manuel', 'Torres', 'Soporte', true),
       ('Lucía', 'Morales', 'Diseñador', true),
       ('Miguel', 'Hernández', 'Desarrollador', true),
       ('Elena', 'Ruiz', 'Analista', false),
       ('Pablo', 'Jiménez', 'Desarrollador', true),
       ('Carmen', 'Navarro', 'Soporte', true),
       ('Raúl', 'Domínguez', 'Gerente', true),
       ('Beatriz', 'Vargas', 'Desarrollador', true),
       ('Francisco', 'Muñoz', 'Soporte', true),
       ('Marta', 'Ortega', 'Diseñador', false),
       ('Andrés', 'Castillo', 'Analista', true),
       ('Isabel', 'Ramos', 'Desarrollador', true); 
````

### 💡 Dato importante — ¿Cómo se ejecutan estos scripts?

Estos scripts no se ejecutan solos. Se ejecutan gracias a la clase `DatabaseConfig` que veremos en el siguiente paso.
Ese bean `ConnectionFactoryInitializer` es quien le dice a Spring: *"al arrancar la aplicación, ejecuta estos dos
archivos SQL en este orden"*.

El orden importa:

````
1️⃣ schema.sql  →  primero crea la estructura (tabla)
2️⃣ data.sql    →  luego inserta los datos 
````

## ⚙️ PASO 4: `DatabaseConfig` y clase principal

### 4.1 — `DatabaseConfig`

¿Cambia algo? ❌ Absolutamente nada. Esta clase es configuración pura de Spring y R2DBC, no tiene ninguna relación con
`Reactor` ni `RxJava`.

````java

@Profile("!test")
@Configuration
public class DatabaseConfig {
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        Resource schema = new ClassPathResource("sql/schema.sql");
        Resource data = new ClassPathResource("sql/data.sql");
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(schema, data);

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(resourceDatabasePopulator);
        return initializer;
    }
}
````

### 4.2 — Clase principal

¿Cambia algo? Solo el nombre de la clase, que `Spring Initializr` genera automáticamente basándose en el `artifactId`
de tu `pom.xml`. En nuestro caso `rxjava-r2dbc` se convierte en `RxjavaR2dbcApplication`.

````java

@SpringBootApplication
public class RxjavaR2dbcApplication {

    public static void main(String[] args) {
        SpringApplication.run(RxjavaR2dbcApplication.class, args);
    }

}
````

## 🏛️ PASO 5: Entidad Employee

### 📖 ¿Cambia algo en las anotaciones o atributos?

❌ Absolutamente nada. Las anotaciones de la entidad son de `Spring Data R2DBC` y `Lombok`, no tienen ninguna relación
con `Reactor` ni `RxJava`.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "employees")
public class Employee {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String position;
    @Column("is_full_time")
    private Boolean fullTime;
}
````

### ¿Por qué eliminamos fromRow() y managerFromRow()?

Estos dos métodos estáticos en tu proyecto original existían para mapear manualmente filas de resultados SQL a objetos
Employee. Se usaban típicamente cuando hacías queries SQL nativas con JOINs complejos donde R2DBC no podía mapear
automáticamente.

````bash
// Ejemplo de cómo se usaban en el proyecto original
.map(row -> Employee.fromRow(row))
.map(row -> Employee.managerFromRow(row))
````

En nuestro proyecto nuevo no los necesitamos porque:

- Nuestro CRUD es simple y directo — sin JOINs complejos.
- `RxJava3CrudRepository` mapea automáticamente los resultados a objetos `Employee`.
- Mantenerlos sería código muerto que solo confundiría.

> 💡 `Regla general`: Si `RxJava3CrudRepository` puede resolver la operación automáticamente, úsalo. Solo necesitarías
> mapeo manual si tuvieras queries SQL nativas muy complejas con JOINs, y en ese caso los métodos de mapeo irían en una
> clase separada, no en la entidad.

## 🗃️ PASO 6: `EmployeeRepository`

````java
public interface EmployeeRepository extends RxJava3CrudRepository<Employee, Integer> {
    Observable<Employee> findByPosition(String position);

    Observable<Employee> findByFullTime(Boolean isFullTime);

    Observable<Employee> findByPositionAndFullTime(String position, Boolean isFullTime);

    Observable<Employee> findByFirstName(String firstName);
}
````

### 📖 Explicación de cada cambio

#### 1️⃣ `R2dbcRepository` → `RxJava3CrudRepository`

Este es el cambio más importante del repositorio. Veamos qué es cada uno:

- Antes — `R2dbcRepository<Employee, Long>`. Era la interfaz de `Spring Data R2DBC` que devuelve tipos `Reactor`.
  Por ejemplo, su método `findAll()` devolvía `Flux<Employee>` y su método `findById()` devolvía `Mono<Employee>`.
- Ahora — `RxJava3CrudRepository<Employee, Long>`. Es la interfaz de `Spring Data R2DBC` con soporte nativo para
  `RxJava 3`. Los mismos métodos ahora devuelven tipos `RxJava`. Por ejemplo, `findAll()` devuelve
  `Observable<Employee>` y `findById()` devuelve `Maybe<Employee>`.

Los dos parámetros genéricos no cambian:

- `Employee` — el tipo de la entidad que maneja.
- `Long` — el tipo del ID de esa entidad.

> 💡 Lo importante aquí es entender que no estamos haciendo ninguna conversión manual. Spring Data internamente sigue
> usando R2DBC, pero nos entrega los resultados directamente en tipos `RxJava`. Eso es exactamente lo que queríamos: 0%
> Reactor visible en nuestro código.

#### 2️⃣ `Flux<Employee>` → `Observable<Employee>`

Todos los métodos que devolvían `Flux` ahora devuelven `Observable`. ¿Por qué `Observable` y no `Flowable`?

| Tipo            | ¿Cuándo usarlo?                                                                                                             |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------|
| `Observable<T>` | Cuando la cantidad de datos es manejable y no necesitas control de velocidad                                                |
| `Flowable<T>`   | Cuando puedes recibir millones de registros y necesitas controlar que el productor no abrume al consumidor `(backpressure)` |

En un CRUD de empleados, la cantidad de registros es predecible y manejable, así que `Observable` es la
elección correcta. Si estuvieras procesando millones de transacciones bancarias en tiempo real, ahí usarías `Flowable`.

#### 3️⃣ ¿Qué métodos nos da `RxJava3CrudRepository` de forma gratuita?

Al extender `RxJava3CrudRepository`, heredamos automáticamente estos métodos sin escribir ni una línea de código:

### Migración de repositorios RxJava a Project Reactor

| Método                | Tipo RxJava            | Tipo Reactor     |
|-----------------------|------------------------|------------------|
| `findAll()`           | `Observable<Employee>` | `Flux<Employee>` |
| `findById(Long id)`   | `Maybe<Employee>`      | `Mono<Employee>` |
| `save(Employee e)`    | `Single<Employee>`     | `Mono<Employee>` |
| `delete(Employee e)`  | `Completable`          | `Mono<Void>`     |
| `deleteById(Long id)` | `Completable`          | `Mono<Void>`     |
| `existsById(Long id)` | `Single<Boolean>`      | `Mono<Boolean>`  |
| `count()`             | `Single<Long>`         | `Mono<Long>`     |

> 💡 `Nota algo muy importante`: `findById()` devuelve `Maybe<Employee>` y no `Single<Employee>`.
> ¿Por qué? Porque `Maybe` representa `0 o 1` resultado — el empleado puede existir o no.
> `Single` en cambio siempre debe emitir exactamente `1` resultado o `un error`, por eso no sería correcto para
> un `findById` donde el registro podría no existir.

## 📦 PASO 7: DTOs y Mapper

### 🔍 ¿Cambia algo respecto al proyecto original?

❌ Absolutamente nada. Los DTOs y el Mapper son clases Java puras que no tienen ninguna relación con Reactor ni RxJava.
Se quedan exactamente igual.

#### 7.1 — `EmployeeRequest`

````java
public record EmployeeRequest(Long id,

                              // Campos que sí requieren validación
                              @NotBlank
                              String firstName,
                              @NotBlank
                              String lastName,
                              @NotBlank
                              String position,
                              @NotNull
                              Boolean fullTime) {
}
````

#### 7.2 — `EmployeeResponse`

````java
public record EmployeeResponse(Long id,
                               String firstName,
                               String lastName,
                               String position,
                               Boolean fullTime) {
}
````

#### 7.3 — `EmployeeMapper`

````java

@Slf4j
@Component
public class EmployeeMapper {
    public EmployeeResponse toEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getPosition(),
                employee.getFullTime()
        );
    }

    public Employee toEmployee(EmployeeRequest employeeRequest) {
        return Employee.builder()
                .id(employeeRequest.id())
                .firstName(employeeRequest.firstName())
                .lastName(employeeRequest.lastName())
                .position(employeeRequest.position())
                .fullTime(employeeRequest.fullTime())
                .build();
    }
}
````
