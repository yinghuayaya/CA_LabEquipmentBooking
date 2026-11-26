package com.yi.labbooking

import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class CommandParserTest {
    @ParameterizedTest
    @CsvSource(
        "equipment add --id E --name P --category AV --location A, ADD",
        "equipment list, LIST", "equipment show --id E, SHOW", "equipment search --query scope, SEARCH",
        "equipment delete --id E, DELETE", "booking cancel --id BK-0001, CANCEL", "booking list, BOOKINGS",
        "help, HELP", "--help, HELP",
    )
    fun `supported commands select the correct action`(line: String, expected: Action) {
        val command = CommandParser.parse(line.split(" "))
        assertEquals(expected, command.action)
        assertEquals(Path.of("data/bookings.json"), command.file)
    }

    @Test
    fun `booking creation preserves quoted values and parses dates`() {
        val command = CommandParser.parse(listOf("booking", "create", "--equipment", "E", "--requester", "Yao Yi",
            "--start", "2026-12-01T09:00", "--end", "2026-12-01T11:00", "--purpose", "Project work"))
        assertEquals(Action.CREATE, command.action)
        val slot = requireNotNull(command.slot)
        assertEquals("Yao Yi", slot.requester)
        assertEquals("Project work", slot.purpose)
        assertEquals("2026-12-01T11:00", slot.endTime.toString())
    }

    @Test
    fun `global option can precede or follow the command with filters`() {
        val words = listOf("booking", "list", "--equipment", "E", "--status", "cancelled")
        val global = listOf("--data-file", "other db.json")
        val command = CommandParser.parse(global + words)
        assertEquals(command, CommandParser.parse(words + global))
        assertEquals(Path.of("other db.json"), command.file)
        assertEquals(Status.CANCELLED, command.status)
        assertEquals("E", command.options["equipment"])
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "equipment", "equipment update", "booking cancel", "equipment show --id", "equipment list --id E",
        "booking list --status invalid", "booking list --equipment --status ACTIVE", "help extra",
        "equipment show --id E --id F", "--data-file", "booking list --status ACTIVE extra",
        "booking create --equipment E --requester Y --start 2026-02-30T10:00 --end 2026-03-01T12:00 --purpose P",
    ])
    fun `invalid commands and options fail during parsing`(line: String) {
        assertThrows(IllegalArgumentException::class.java) { CommandParser.parse(line.split(" ")) }
    }
}
