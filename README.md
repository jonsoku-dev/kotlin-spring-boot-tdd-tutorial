# 0. 유용한 라이브 템플릿
## test
kotlin : class
```
@Test
fun `should $DESCRIPTION$`() {
    // given
    $GIVEN$
    
    // when
    $WHEN$
    
    // then
    $THEN$
}
```
## nested
Kotlin : class, object declaration
```
@Nested
@DisplayName("$DISPLAY_NAME$")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
inner class $CLASS_NAME$ {
    
    $BODY$
}
```

# 1. Datasource
보통 내가 사용하는 단어는 `Repository` 이다.
하지만 여기서는 `Datasource` 로 사용하겠다.
## BankDataSource
`interface` 이다.
```kotlin
package com.example.thenewboston.datasource

import com.example.thenewboston.model.Bank

interface BankDataSource {
    fun retrieveBanks(): Collection<Bank>
}
```
## MockBankDataSource
보통 구현체를 만들지만 TDD 용 Mock 을 만들어보자
```kotlin
package com.example.thenewboston.datasource.mock

import com.example.thenewboston.datasource.BankDataSource
import com.example.thenewboston.model.Bank
import org.springframework.stereotype.Repository

@Repository
class MockBankDataSource : BankDataSource {

    val banks = listOf(
            Bank("123", 12.7, 1),
            Bank("efa", 17.7, 0),
            Bank("555", 11.7, 3)
    )

    override fun retrieveBanks(): Collection<Bank> = banks
}
```
## MockBankDataSourceTest
위에서 구현한 `MockBankDataSource` 을 테스트하기 위해서 
`MockBankDataSourceTest`를 만들자
```kotlin
package com.example.thenewboston.datasource.mock

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MockBankDataSourceTest {
    private val mockDataSource = MockBankDataSource()

    @Test
    fun `should provide a collection of banks`() {
        // when
        val banks = mockDataSource.retrieveBanks()

        // then
        assertThat(banks.size).isGreaterThanOrEqualTo(3)
    }

    @Test
    fun `should provide some mock data`() {
        // when
        val banks = mockDataSource.retrieveBanks()

        // then
        assertThat(banks).allMatch { it.accountNumber.isNotBlank() }
        assertThat(banks).anyMatch { it.trust != 0.0 }
        assertThat(banks).anyMatch { it.transactionFee != 0 }
    }
}
```
* 먼저 순수 객체를 테스트 하기 위해서 스프링부트테스트는 사용하지 않는다.
* `allMatch` : 모두 만족해야함
* `anyMatch` : 하나만 만족하면 됨

# 2. Service
보통 비지니스 로직이 들어간다.
## BankService
```kotlin
package com.example.thenewboston.service

import com.example.thenewboston.datasource.BankDataSource
import com.example.thenewboston.model.Bank
import org.springframework.stereotype.Service

@Service
class BankService(private val dataSource: BankDataSource) {
    fun getBanks(): Collection<Bank> = dataSource.retrieveBanks()
}
```
* 여기서는 단순 datasource 에서 받아온 데이터를 바인딩해주는 수준이다.
* 순수 객체를 테스트하고 싶은데 생성자에 `private val dataSource: BankDataSource` 이놈이 들어왔다. 이건 어떻게해야될까?
* 다음 테스트코드에서 확인해보자

## BankServiceTest
```kotlin
package com.example.thenewboston.service

import com.example.thenewboston.datasource.BankDataSource
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class BankServiceTest {

    private val dataSource: BankDataSource = mockk(relaxed = true)
    private val bankService = BankService(dataSource)

    @Test
    fun `should call its data source to retrieve banks`() {
        // given
//        every { dataSource.retrieveBanks() } returns emptyList()

        // when
        bankService.getBanks();
        // then
        verify(exactly = 1) { dataSource.retrieveBanks() }
    }
}
```
* 순수 객체를 테스트하기 위해서 다른 객체를 DI 하지 않고, Mock 객체를 DI 한다.
* https://javacan.tistory.com/entry/kotlin-mock-framework-mockk-intro
* `mockk(relaxed = true)` 과 `every { dataSource.retrieveBanks() } returns emptyList()` 는 같다.
* `verify(exactly = 1) { dataSource.retrieveBanks() }` : dataSource 에서의 호출여부를 확인 ?

# 3. Controller
대망의 컨트롤러 작성 방법 및 테스트 방법이다. 매우 깔끔한 것 같다.
## BankController
```kotlin
package com.example.thenewboston.controller

import com.example.thenewboston.model.Bank
import com.example.thenewboston.service.BankService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/banks")
class BankController(private val service: BankService) {
    @GetMapping
    fun getBanks(): Collection<Bank> = service.getBanks()
}
```
```kotlin
val banks = listOf(
            Bank("123", 12.7, 1),
            Bank("efa", 17.7, 0),
            Bank("555", 11.7, 3)
    )
```
* 이 banks 를 json 으로 받는 Controller 이다.

## BankControllerTest
```kotlin
package com.example.thenewboston.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
internal class BankControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should return all banks`() {
        // when/then
        mockMvc.get("/api/banks")
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$[0].accountNumber") { value("123") }
                }
    }
}
```
* 컨트롤러 테스트는 통신테스트? 이기 때문에 스프링기능을 써야하나보다. `@SpringBootTest` 를 이용하자.
* `@AutoConfigureMockMvc` 와 `@Autowired lateinit var mockMvc: MockMvc` 는 한몸이다.
* `andDo` : get 을 요청하고 이후 동작에 대해서 print 한다.
* `andExpect` : 기대하는것
    * `status` : status 상태
    * `content` : Content 는 어떤 타입인지 
    * `jsonPath` 
        * 배열일때 : $[0].xxx.yyy.zzz
        * 오브젝트일때 : $.xxx.yyy.zzz
* 더 많은 정보는 
    * https://mkyong.com/spring-boot/spring-test-how-to-test-a-json-array-in-jsonpath/
    * https://stackoverflow.com/questions/25614593/asserting-array-of-arrays-with-jsonpath-and-spring-mvc
    
## Nested 로 감싼 테스트 구문
테스트코드를 더 직관적으로 볼 수 있다. 
```kotlin
package com.example.thenewboston.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
internal class BankControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Nested
    @DisplayName("getBanks()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetBanks {

        @Test
        fun `should return all banks`() {
            // when/then
            mockMvc.get("/api/banks")
                    .andDo { print() }
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        jsonPath("$[0].accountNumber") { value("1234") }
                    }
        }
    }

    @Nested
    @DisplayName("getBank()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetBank {
        @Test
        fun `should return the bank with the given account number`() {
            // given
            val accountNumber = 1234

            // when/then
            mockMvc.get("/api/banks/$accountNumber")
                    .andDo { print() }
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        jsonPath("$.trust") { value(12.7) }
                        jsonPath("$.transactionFee") { value(1) }
                    }
        }
        
        @Test
        fun `should return NOT FOUND if the account number does not exist`() {
            // given
            val accountNumber = "does_not_exist"

            // when/then
            mockMvc.get("/api/banks/$accountNumber")
                    .andDo { print() }
                    .andExpect {
                        status { isNotFound() }
                    }
        }
    }
}
```

## @ExceptionHandler
Exception 을 받아서 컨트롤러 단에서 커스터마이징할 수 있다.
### .first 메소드
`MockBankDataSource` 에서  `banks.first { it.accountNumber == accountNumber }`
구분이 있는데, 이 first 는 값을 찾지 못하면, `NoSuchElementException` 를 발생시킨다.
이것을 Controller 에서 캐치하여 404 에러로 내보낼 수 있다.
### BankController 
```kotlin
@RestController
@RequestMapping("/api/banks")
class BankController(private val service: BankService) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<String> = ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    ...

}
```
하지만 다른 컨트롤러에서도 사용하고 있다면, `@ExceptionHandler` 를 계속해서 컨트롤러에 작성해야하는데, 이것은 매우 불편하다.
그렇다면 이걸 어떻게 공통적으로 사용할 수 있도록 개선시킬 수 있을까?
이것은 `@ControllerAdvice` 를 사용하면 된다. 나중에 한번 해보자.

## MockBankDatSource
일단 에러메시지를 커스터마이징 해보자! `first` -> `firstOrNull` 로 변경한 후에, 

엘비스연산자를 사용해서 값이 없다면 throw 한다.
```kotlin
package com.example.thenewboston.datasource.mock

import com.example.thenewboston.datasource.BankDataSource
import com.example.thenewboston.model.Bank
import org.springframework.stereotype.Repository

@Repository
class MockBankDataSource : BankDataSource {

    val banks = listOf(
            Bank("1234", 12.7, 1),
            Bank("efa", 17.7, 0),
            Bank("555", 11.7, 3)
    )

    override fun retrieveBanks(): Collection<Bank> = banks

    override fun retrieveBank(accountNumber: String): Bank = banks.firstOrNull { it.accountNumber == accountNumber }
            ?: throw NoSuchElementException("Could not find a bank with account number $accountNumber")
}
```
