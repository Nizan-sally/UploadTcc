package com.example.tcc3.utils

import android.content.Context
import android.widget.Toast
import com.example.tcc3.R

class InputManager (viewContext: Context) {

    private val context = viewContext

    fun hasEmptyData(dataList: List<String>): Boolean {
        var hasEmpty = false
        for (data in dataList) {
            if (data.isEmpty()) {
                hasEmpty = true
            }
        }
        if (hasEmpty) {
            Toast.makeText(context, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show()
        }
        return hasEmpty
    }

    fun isEmailCorrectlyFormatted(email: String): Boolean {
        val condition = email.contains("@") && email.contains(".")
        if (!condition) {
            Toast.makeText(context, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show()
        }
        return condition
    }

    fun doesConfirmPasswordMatch(password: String, confirmPassword: String): Boolean {
        val condition = (password == confirmPassword)
        if (!condition) {
            Toast.makeText(context, R.string.msg_senhas_diferentes, Toast.LENGTH_SHORT).show()
        }
        return condition
    }
    fun isPasswordStrong(password: String): Boolean {
        // Senha deve conter pelo menos: um número, uma letra minúscula, uma letra maiúscula, um caractere especial;
        // e ter, no mínimo, 8 dígitos
        val passwordRequirements = ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
        val condition = password.matches(passwordRequirements.toRegex())
        if (!condition) {
            Toast.makeText(context, "A senha deve ter no mínimo 8 caracteres e conter pelo menos um número, uma letra minúscula, uma letra maiúscula e um caractere especial", Toast.LENGTH_LONG).show()
        }
        return condition
    }
}