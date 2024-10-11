package dev.pbt.casigma.modules

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

class Argon2() {
    val encoder = Argon2PasswordEncoder(16, 32, 1, 60000,10)

    fun hash(text: String): String {
        return encoder.encode(text)
    }

    fun verify(raw: String, encoded: String): Boolean {
        return encoder.matches(raw, encoded)
    }
}