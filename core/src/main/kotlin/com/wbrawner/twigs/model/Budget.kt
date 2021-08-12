package com.wbrawner.twigs.model

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.randomString

data class Budget(
    override val id: String = randomString(),
    var name: String? = null,
    var description: String? = null,
    var currencyCode: String? = "USD",
) : Identifiable
