package com.wbrawner.twigs.server.session

import com.wbrawner.budgetserver.randomString
import com.wbrawner.budgetserver.twoWeeksFromNow
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Session(val userId: String = "") {
    @Id
    val id = randomString()
    val token = randomString(255)
    var expiration = twoWeeksFromNow
}

data class SessionResponse(val token: String, val expiration: String) {
    constructor(session: Session) : this(session.token, session.expiration.toInstant().toString())
}
