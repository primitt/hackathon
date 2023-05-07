package dev.primitt.dto

data class ServerPreference(val allergies: Array<String>, val diet: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerPreference

        if (!allergies.contentEquals(other.allergies)) return false
        return diet == other.diet
    }

    override fun hashCode(): Int {
        var result = allergies.contentHashCode()
        result = 31 * result + diet.hashCode()
        return result
    }
}