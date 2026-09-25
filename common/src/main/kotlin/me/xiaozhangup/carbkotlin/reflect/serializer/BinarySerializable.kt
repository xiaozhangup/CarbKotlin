package me.xiaozhangup.carbkotlin.reflect.serializer

interface BinarySerializable {

    /**
     * 写入到 ByteBuffer
     */
    fun writeTo(writer: BinaryWriter)
}