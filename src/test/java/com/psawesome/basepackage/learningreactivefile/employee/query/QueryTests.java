package com.psawesome.basepackage.learningreactivefile.employee.query;

import com.psawesome.basepackage.learningreactivefile.employee.dto.Employee;
import com.psawesome.basepackage.learningreactivefile.employee.repo.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.startsWith;
import static org.springframework.data.mongodb.core.query.Criteria.byExample;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * package: com.psawesome.basepackage.learningreactivefile.employee.query
 * author: PS
 * DATE: 2020-01-05 일요일 07:50
 */
@DisplayName("Employee Query 테스트")
@DataMongoTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueryTests {

    final EmployeeRepository repository;

    final ReactiveMongoOperations operations;

    final MongoOperations mongoOperations;

    @Autowired
    public QueryTests(EmployeeRepository repository, ReactiveMongoOperations operations, MongoOperations mongoOperations) {
        this.repository = repository;
        this.operations = operations;
        this.mongoOperations = mongoOperations;
    }

    @Test
    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection(Employee.class);

        Employee e1 = new Employee();
        e1.setId(UUID.randomUUID().toString());
        e1.setFirstName("Bilbo");
        e1.setLastName("Baggins");
        e1.setRole("burglar");

        mongoOperations.insert(e1);

        Employee e2 = new Employee();
        e2.setId(UUID.randomUUID().toString());
        e2.setFirstName("Frodo");
        e2.setLastName("Baggins");
        e2.setRole("ring bearer");

        mongoOperations.insert(e2);

    }

    @DisplayName("testSingle")
    @ParameterizedTest(name = "For Example name : {0}")
    @ValueSource(strings = {"Bilbo"})
    void repository_싱글(String name) {
        Employee e = new Employee();
        e.setFirstName(name);
        Example<Employee> example = Example.of(e);

        Mono<Employee> singleEmployee = repository.findOne(example);

        StepVerifier.create(singleEmployee)
            .expectNextMatches(employee -> {
                assertAll(
                    () -> assertThat(employee).hasNoNullFieldsOrProperties(),
                    () -> assertEquals("Bilbo", employee.getFirstName()),
                    () -> assertEquals("Baggins", employee.getLastName()),
                    () -> assertEquals("burglar", employee.getRole())
                );
                return true;
            });
    }

    @DisplayName("testMultiple")
    @Test
    void repository_멀티() {
        Employee e = new Employee();
        e.setLastName("baggins");

        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnoreCase()
            .withMatcher("lastName", startsWith())
            .withIncludeNullValues();

        Example<Employee> example = Example.of(e, matcher);

        Flux<Employee> all = repository.findAll(example);

        StepVerifier.create(all.collectList())
            .expectNextMatches(employee -> {
                System.out.println("employee = " + employee);
                assertAll(
                    () -> assertSame(2, employee.size(), "총 size"),
                    () -> assertIterableEquals(List.of("Bilbo", "Frodo"), employee.stream().map(emp -> emp.getFirstName()).collect(Collectors.toList()))
                );
                return true;
            })
            .expectComplete()
            .verify();
    }


    @Test
    @DisplayName("Single WIth Template")
    public void 싱글_템플릿() {
        Employee e = new Employee();
        e.setFirstName("Bilbo");
        Example<Employee> example = Example.of(e);

        Mono<Employee> singleEmployee = operations.findOne(new Query(byExample(example)), Employee.class);

        StepVerifier.create(singleEmployee)
            .expectNextMatches(employee -> {
                assertAll(
                    () -> assertThat(employee).hasNoNullFieldsOrProperties(),
                    () -> assertEquals("Bilbo", employee.getFirstName()),
                    () -> assertEquals("Baggins", employee.getLastName()),
                    () -> assertEquals("burglar", employee.getRole())
                );
                return true;
            })
            .expectComplete()
            .verify();

    }

    @DisplayName("멀티 Template 테스트")
    @Test
    void testMultipleWIthTemplate() {
        Employee e = new Employee();
        e.setLastName("baggins"); // Lowercase lastName

        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnoreCase()
            .withMatcher("lastName", startsWith())
            .withIncludeNullValues();

        Example<Employee> example = Example.of(e, matcher);

        Flux<Employee> multipleEmployees = operations.find(
            new Query(byExample(example)), Employee.class);

        StepVerifier.create(multipleEmployees.collectList())
            .expectNextMatches(employees -> {
                assertThat(employees).hasSize(2);
                assertThat(employees).extracting("firstName")
                    .contains("Frodo", "Bilbo");
                return true;
            })
            .expectComplete()
            .verify();
    }

    @Test
    void name() {
        Mono<Employee> one = operations.findOne(
            Query.query(where("firstName").is("Frodo")), Employee.class
        );
        long frodo = StepVerifier.create(one)
            .expectNextMatches(employee -> {
                assertAll(
                    () -> assertEquals("Frodo", employee.getFirstName())
                );
                return true;
            })
            .expectComplete()
            .verify()
            .getSeconds();

        System.out.println("frodo = " + frodo);
    }


    /*

    @BeforeAll
    public static void setUp(TestInfo testInfo) {
        System.out.println("DisplayName = " + testInfo.getDisplayName());
        System.out.println("QueryTests.class.getName() = " + QueryTests.class.getName());
    }

    @ParameterizedTest(name = "For example, year {0} is not supported.")
    @ValueSource(strings = {"hello", "world", "Flux", "Mono"})
    public void dummy(String candidate) {
        System.out.println("candidate = " + candidate);
    }
*/

    @Configuration
    @EnableReactiveMongoRepositories(basePackageClasses = EmployeeRepository.class)
    static class TestConfig {

    }
}
