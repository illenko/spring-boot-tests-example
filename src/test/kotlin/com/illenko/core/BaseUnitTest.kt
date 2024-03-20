package com.illenko.core

import io.mockk.junit5.MockKExtension
import io.mockk.junit5.MockKExtension.CheckUnnecessaryStub
import io.mockk.junit5.MockKExtension.ConfirmVerification
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith

@Tag("UnitTest")
@ExtendWith(MockKExtension::class)
@ConfirmVerification
@CheckUnnecessaryStub
abstract class BaseUnitTest : BaseTest()
