package com.yi.labbooking

import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import java.time.DateTimeException
import kotlinx.serialization.json.Json

class BookingStore(path: Path) {
    private val file = path.toAbsolutePath().normalize()
    private val json = Json { prettyPrint = true }

    fun <T> read(action: (BookingService) -> T): T = access(false, action)
    fun <T> update(action: (BookingService) -> T): T = access(true, action)

    private fun <T> access(write: Boolean, action: (BookingService) -> T): T {
        require(file.fileName != null) { "Expected a data file path." }
        Files.createDirectories(file.parent)
        val lock = file.resolveSibling("${file.fileName}.lock")
        return FileChannel.open(lock, CREATE, WRITE).use { channel ->
            channel.lock().use {
                val state = load()
                val result = action(state)
                if (write) save(state)
                result
            }
        }
    }

    private fun load(): BookingService {
        val content = try {
            Files.readString(file)
        } catch (_: NoSuchFileException) {
            return BookingService().also(::save)
        }
        return try {
            json.decodeFromString<DatabaseDto>(content).restore()
        } catch (error: IllegalArgumentException) {
            throw IOException("Invalid database at $file: ${error.message}", error)
        } catch (error: DateTimeException) {
            throw IOException("Invalid date in $file: ${error.message}", error)
        }
    }

    private fun save(state: BookingService) {
        val content = json.encodeToString(DatabaseDto.from(state))
        val temp = Files.createTempFile(file.parent, ".lab-booking-", ".tmp")
        try {
            Files.writeString(temp, content)
            try {
                Files.move(temp, file, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temp, file, REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temp)
        }
    }
}
