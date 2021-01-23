package com.example.thenewboston.datasource

import com.example.thenewboston.model.Bank

interface BankDataSource {
    fun retrieveBanks(): Collection<Bank>
    fun retrieveBank(accountNumber: String): Bank
}