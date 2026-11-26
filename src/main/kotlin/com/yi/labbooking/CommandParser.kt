package com.yi.labbooking

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

enum class Action(val label: String, val required: Set<String> = emptySet(), val optional: Set<String> = emptySet()) {
    ADD("equipment add", setOf("id", "name", "category", "location")),
    LIST("equipment list"),
    SHOW("equipment show", setOf("id")),
    SEARCH("equipment search", setOf("query")),
    DELETE("equipment delete", setOf("id")),
    CREATE("booking create", setOf("equipment", "requester", "start", "end", "purpose")),
    BOOKINGS("booking list", optional = setOf("equipment", "status")),
    CANCEL("booking cancel", setOf("id")),
    HELP("help"),
}

data class Command(
    val action: Action,
    val file: Path,
    val options: Map<String, String>,
    val slot: Reservation?,
    val status: Status?,
)

object CommandParser {
    fun parse(args: List<String>): Command {
        val words = mutableListOf<String>()
        val options = mutableMapOf<String, String>()
        val tokens = args.iterator()
        while (tokens.hasNext()) {
            val token = tokens.next()
            if (token.startsWith("--") && token != "--help") {
                val key = token.removePrefix("--")
                require(tokens.hasNext()) { "Missing value for $token." }
                val value = tokens.next()
                require(value.isNotBlank() && !value.startsWith("--")) { "Missing value for $token." }
                require(options.put(key, value) == null) { "Duplicate option $token." }
            } else {
                words.add(token)
            }
        }
        val file = Path.of(options.remove("data-file") ?: "data/bookings.json")
        val label = words.joinToString(" ")
        val action = if (label == "--help") Action.HELP else requireNotNull(Action.entries.find { it.label == label }) {
            "Unknown command '$label'. Use help."
        }
        validate(action, options)
        return Command(action, file, options.toMap(), reservation(action, options), options["status"]?.let(::status))
    }

    private fun validate(action: Action, options: Map<String, String>) {
        val unknown = options.keys - action.required - action.optional
        require(unknown.isEmpty()) { "Unexpected option --${unknown.first()} for ${action.label}." }
        val missing = action.required - options.keys
        require(missing.isEmpty()) { "Missing option --${missing.first()} for ${action.label}." }
    }

    private fun reservation(action: Action, options: Map<String, String>) =
        if (action == Action.CREATE) Reservation(
            options.getValue("equipment"), options.getValue("requester"), date(options.getValue("start")),
            date(options.getValue("end")), options.getValue("purpose"),
        ) else null

    private fun date(value: String): LocalDateTime = try {
        LocalDateTime.parse(value)
    } catch (error: DateTimeParseException) {
        throw IllegalArgumentException("Invalid local date/time '$value'. Example: 2026-12-01T09:00.", error)
    }

    private fun status(value: String) = requireNotNull(Status.entries.find { it.name == value.uppercase() }) {
        "Invalid status '$value'. Use ACTIVE or CANCELLED."
    }
}
