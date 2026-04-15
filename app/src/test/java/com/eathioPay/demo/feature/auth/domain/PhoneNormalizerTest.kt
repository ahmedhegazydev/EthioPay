package com.eathioPay.demo.feature.auth.domain

import com.eathioPay.demo.feature.auth.domain.usecase.PhoneNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNormalizerTest {

    @Test
    fun `normalizes egypt local numbers`() {
        assertEquals("+201156749640", PhoneNormalizer.normalize("01156749640"))
    }

    @Test
    fun `normalizes arabic digits`() {
        assertEquals("+201156749640", PhoneNormalizer.normalize("٠١١٥٦٧٤٩٦٤٠"))
    }

    @Test
    fun `returns null for blank input`() {
        assertNull(PhoneNormalizer.normalize("   "))
    }
}
