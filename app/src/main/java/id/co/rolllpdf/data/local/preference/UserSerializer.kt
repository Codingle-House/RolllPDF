package id.co.rolllpdf.data.local.preference

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import id.co.rolllpdf.datastore.UserPreference
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by pertadima on 07,March,2021
 */
object UserSerializer : Serializer<UserPreference> {
    override suspend fun readFrom(input: InputStream): UserPreference {
        try {
            return UserPreference.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Error deserializing proto", exception)
        }
    }

    override suspend fun writeTo(t: UserPreference, output: OutputStream) = t.writeTo(output)

    override val defaultValue: UserPreference
        get() = UserPreference.getDefaultInstance()
}