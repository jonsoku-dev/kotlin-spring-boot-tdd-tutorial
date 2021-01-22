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

/*
 * https://javacan.tistory.com/entry/kotlin-mock-framework-mockk-intro
 * mockk(relaxed = true) 과 every { dataSource.retrieveBanks() } returns emptyList() 는 같다.
 * verify(exactly = 1) { dataSource.retrieveBanks() } : dataSource 에서의 호출여부를 확인 ?
 */