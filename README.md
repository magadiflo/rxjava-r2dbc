# 🔗 Spring Boot | RxJava | R2DBC

### Contexto

Este proyecto es la adaptación del proyecto
[webflux-r2dbc-joins-test](https://github.com/magadiflo/webflux-r2dbc-joins-test.git) donde trabajamos con
`webFlux`. Sin embargo, en este proyecto `rxjava-r2dbc` vamos a adaptar parte del proyecto anterior pero al estilo de
`rxJava` como reemplazo de `Reactor (Mono, Flux)`. Esto lo hacemos porque en muchas ofertas laborales piden `rxJava`,
aunque `Reactor` es la opción moderna y recomendada por el equipo de `Spring`, crearé este proyecto para ver cómo
se trabaja con `rxJava`.

---

## 📦 PASO 1: pom.xml — Explicación completa

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

