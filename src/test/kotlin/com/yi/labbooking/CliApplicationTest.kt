package com.yi.labbooking

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CliApplicationTest(@TempDir val directory: Path) {
    private val file = directory.resolve("data.json")
    private val output = ByteArrayOutputStream()
    private val errors = ByteArrayOutputStream()

    private fun run(vararg args: String): Int {
        output.reset()
        errors.reset()
        val app = CliApplication(PrintStream(output), PrintStream(errors))
        return app.run(listOf("--data-file", file.toString()) + args)
    }

    @Test
    fun `help and malformed arguments do not touch storage`() {
        assertEquals(0, run("--help"))
        assertTrue(output.toString().contains("booking create"))
        assertEquals(1, run("equipment", "add"))
        assertTrue(errors.toString().contains("Missing option"))
        assertFalse(Files.exists(file))
    }

    @Test
    fun `catalog commands and booking lifecycle work across invocations`() {
        assertEquals(0, run("equipment", "add", "--id", "EQ", "--name", "Projector",
            "--category", "AV", "--location", "Lab A"))
        for (args in listOf(listOf("list"), listOf("show", "--id", "EQ"), listOf("search", "--query", "proj"))) {
            assertEquals(0, run("equipment", *args.toTypedArray()))
            assertTrue(output.toString().contains("EQ | Projector | AV | Lab A"))
        }
        val create = arrayOf("booking", "create", "--equipment", "EQ", "--requester", "Yao Yi", "--start",
            "2026-12-01T09:00", "--end", "2026-12-01T11:00", "--purpose", "Project work")
        assertEquals(0, run(*create))
        assertTrue(output.toString().contains("BK-0001"))
        assertEquals(1, run(*create))
        assertTrue(errors.toString().contains("Overlapping"))
        assertEquals(0, run("booking", "cancel", "--id", "BK-0001"))
        assertEquals(0, run("booking", "list", "--equipment", "EQ", "--status", "CANCELLED"))
        assertTrue(output.toString().contains("BK-0001"))
        assertTrue(output.toString().contains("CANCELLED"))
        assertEquals(0, run(*create))
        assertTrue(output.toString().contains("BK-0002"))
        assertEquals(1, run("equipment", "delete", "--id", "EQ"))
    }

    @Test
    fun `deleting unused equipment persists an empty catalog`() {
        assertEquals(0, run("equipment", "add", "--id", "A", "--name", "P", "--category", "AV", "--location", "B"))
        assertEquals(0, run("equipment", "delete", "--id", "A"))
        assertEquals(0, run("equipment", "list"))
        assertEquals("No equipment.", output.toString().trim())
    }

    @Test
    fun `storage failures go to stderr and preserve the database`() {
        Files.writeString(file, "{")
        assertEquals(1, run("booking", "list"))
        assertTrue(errors.toString().contains("Invalid database"))
        assertTrue(output.toString().isEmpty())
        assertEquals("{", Files.readString(file))
    }
}
