package com.example.thenewboston.datasource.network.dto

import com.example.thenewboston.model.Bank

data class BankList(
        val results: Collection<Bank>
) // Bank 도 DTO 를 따로 만들어서 넣는게 낫다. 예제라서 그냥 엔티티를 바로 넣음