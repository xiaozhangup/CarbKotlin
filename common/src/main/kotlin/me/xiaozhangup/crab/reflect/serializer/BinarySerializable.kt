package me.xiaozhangup.crab.reflect.serializer

interface BinarySerializable {

    /**
     * 写入到 ByteBuffer
     */
    fun writeTo(writer: BinaryWriter)
}