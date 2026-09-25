package me.xiaozhangup.crab.serialization

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class ByteWriter(private val output: DataOutputStream) {
    fun writeInt(value: Int) = output.writeInt(value)
    fun writeLong(value: Long) = output.writeLong(value)
    fun writeShort(value: Short) = output.writeShort(value.toInt())
    fun writeByte(value: Byte) = output.writeByte(value.toInt())
    fun writeFloat(value: Float) = output.writeFloat(value)
    fun writeDouble(value: Double) = output.writeDouble(value)
    fun writeBoolean(value: Boolean) = output.writeBoolean(value)
    fun writeString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeInt(bytes.size)
        output.write(bytes)
    }

    fun writeUUID(uuid: UUID) {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }

    fun writeByteArray(bytes: ByteArray) {
        writeInt(bytes.size)
        output.write(bytes)
    }

    fun <T> writeCollection(list: Collection<T>, writer: (T, ByteWriter) -> Unit) {
        writeInt(list.size)
        for (item in list) {
            writer(item, this)
        }
    }

    fun <K, V> writeMap(map: Map<K, V>, writer: (K, V, ByteWriter) -> Unit) {
        writeInt(map.size)
        for ((k, v) in map) {
            writer(k, v, this)
        }
    }

    fun writeComponent(component: Component) {
        writeString(GsonComponentSerializer.gson().serialize(component))
    }
}

class ByteReader(private val input: DataInputStream) {
    fun remaining(): Int = input.available()
    fun readInt(): Int = input.readInt()
    fun readLong(): Long = input.readLong()
    fun readShort(): Short = input.readShort()
    fun readByte(): Byte = input.readByte()
    fun readFloat(): Float = input.readFloat()
    fun readDouble(): Double = input.readDouble()
    fun readBoolean(): Boolean = input.readBoolean()
    fun readString(): String {
        val length = readInt()
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    fun readUUID(): UUID {
        val mostSignificantBits = readLong()
        val leastSignificantBits = readLong()
        return UUID(mostSignificantBits, leastSignificantBits)
    }

    fun readByteArray(): ByteArray {
        val length = readInt()
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return bytes
    }

    fun <T> readCollection(reader: (ByteReader) -> T): List<T> {
        val size = readInt()
        return List(size) { reader(this) }
    }

    fun <K, V> readMap(reader: (ByteReader) -> Pair<K, V>): Map<K, V> {
        val size = readInt()
        val map = mutableMapOf<K, V>()
        repeat(size) {
            val (key, value) = reader(this)
            map[key] = value
        }
        return map
    }

    fun readComponent(): Component {
        return GsonComponentSerializer.gson().deserialize(
            readString()
        )
    }
}

/**
 * 构造一个 ByteArray，并可选是否进行 GZIP 压缩
 * @param compress 是否压缩
 * @param block 在 ByteWriter 上执行写操作
 */
fun byteArray(compress: Boolean = false, block: ByteWriter.() -> Unit): ByteArray {
    val baos = ByteArrayOutputStream()
    val dos = DataOutputStream(baos)
    ByteWriter(dos).block()
    dos.flush()
    val data = baos.toByteArray()
    return if (compress) gzipCompress(data) else data
}

/**
 * 从 ByteArray 中读取数据，可选是否先进行 GZIP 解压
 * @param data 输入字节数组
 * @param compressed 是否已压缩
 * @param block 在 ByteReader 上执行读操作，返回自定义结果
 */
fun <R> byteArray(
    data: ByteArray, compressed: Boolean = false, block: ByteReader.() -> R
): R {
    val inputBytes = if (compressed) gzipDecompress(data) else data
    val bais = ByteArrayInputStream(inputBytes)
    val dis = DataInputStream(bais)
    return ByteReader(dis).block()
}

private fun gzipCompress(data: ByteArray): ByteArray {
    val baos = ByteArrayOutputStream()
    GZIPOutputStream(baos).use { it.write(data) }
    return baos.toByteArray()
}

private fun gzipDecompress(data: ByteArray): ByteArray {
    val bais = ByteArrayInputStream(data)
    GZIPInputStream(bais).use { gis ->
        return gis.readBytes()
    }
}