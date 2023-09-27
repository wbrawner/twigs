package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.PasswordResetToken
import com.wbrawner.twigs.storage.PasswordResetRepository

class FakePasswordResetRepository : FakeRepository<PasswordResetToken>(), PasswordResetRepository