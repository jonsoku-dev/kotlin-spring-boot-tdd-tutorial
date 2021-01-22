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