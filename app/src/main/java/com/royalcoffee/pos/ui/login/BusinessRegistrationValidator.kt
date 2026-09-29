package com.royalcoffee.pos.ui.login

// This file contains validation error messages for the business registration form

data class BusinessValidationErrors(
    val businessName: String? = null,
    val brn: String? = null,
    val vat: String? = null,
    val country: String? = null,
    val email: String? = null

) {
    // The form is valid only when every field has no error.
    val isValid: Boolean
        get() =
            businessName == null &&
                    brn == null &&
                    vat == null &&
                    country == null &&
                    email == null

    // General message for ViewModel is registration is attempted with invalid information
    fun firstErrorMessage(): String {
        return businessName
            ?: brn
            ?: vat
            ?: country
            ?: email
            ?: "Please ensure that the business information is entered correctly."
    }
}

// Validates business information entered during first-run registration
// 1, RegistrationScreen, before moving to the Admin step
// 2. RegistrationViewModel before saving the registration data.
// This precents us from implementing different rules in different parts of the application
object BusinessRegistrationValidator {
    private val BRN_REGEX = Regex("^[A-Za-z][0-9]{9}$") // Exactly one letter followed by 9 digits
    private val VAT_REGEX = Regex("^[0-9]{8}$") // 9 number


    // Check Business Registration field
    fun validate(
        businessName: String,
        brn: String,
        vat: String,
        country: String,
        email: String
    ): BusinessValidationErrors {

        // trimming the input data
        val cleanedBusinessName = businessName.trim()
        val cleanedBrn = brn.trim()
        val cleanedVat = vat.trim()
        val cleanedCountry = country.trim()
        val cleanedEmail = email.trim()

        // Event when the business name is empty
        val businessNameError = when {
            cleanedBusinessName.isBlank() -> { "Business name is required." }
            else -> { null }
        }

        // BRN validation starts here
        // Error message when BRN is empty
        val brnError = when {cleanedBrn.isBlank() -> { "Business Registration Number is required." }
            // Error message when BRN does not match the regex pattern
            !BRN_REGEX.matches(cleanedBrn) -> { "BRN must contain 1 letter followed by exactly 9 digits." }

            // Else return null
            else -> { null }
        }

        // VAT Validation
        val vatError = when {cleanedVat.isBlank() -> { "VAT number is required." }

            !VAT_REGEX.matches(cleanedVat) -> {
                "VAT number must contain exactly 8 digits."
            }
            else -> {
                null
            }
        }

        // Country validation
        val countryError = when {cleanedCountry.isBlank() -> { "Country is required." }
            else -> {
                null
            }
        }

        // Email validation - Basic - '@' is required
        val emailError = when {cleanedEmail.isBlank() -> { "Business email is required." }

            !cleanedEmail.contains("@") -> {
                "Business email must contain @."
            }
            else -> {
                null
            }
        }

        // Return the validation errors
        return BusinessValidationErrors(
            businessName = businessNameError,
            brn = brnError,
            vat = vatError,
            country = countryError,
            email = emailError
        )
    }
}