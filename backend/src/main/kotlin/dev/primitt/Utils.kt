package dev.primitt

import java.security.MessageDigest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


fun sha256(base: String): String? {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(base.toByteArray(charset("UTF-8")))
        val hexString = StringBuilder()
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        hexString.toString()
    } catch (ex: Exception) {
        throw RuntimeException(ex)
    }
}
fun checkLogin(session: String, uuid: String): Boolean? {
    val sessions = Sessions.select { Sessions.sessionId eq session }
    if (sessions.count() == 0L){
        return false
    }
    else{
        val firstSession = sessions.first()
        if (firstSession[Sessions.uuid] == uuid){
            return true
        }
        else{
            return false
        }
    }
}