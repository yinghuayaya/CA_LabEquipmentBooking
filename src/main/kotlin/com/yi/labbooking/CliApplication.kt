package com.yi.labbooking

import java.io.IOException
import java.io.PrintStream

class CliApplication(private val out: PrintStream = System.out, private val err: PrintStream = System.err) {
    fun run(args: List<String>): Int = try {
        out.println(execute(CommandParser.parse(args)))
        0
    } catch (error: IllegalArgumentException) {
        err.println("Error: ${error.message}")
        1
    } catch (error: IOException) {
        err.println("Storage error: ${error.message}")
        1
    }

    private fun execute(command: Command): String {
        val store = BookingStore(command.file)
        val values = command.options
        return when (command.action) {
            Action.HELP -> help()
            Action.ADD -> store.update {
                val item = Equipment(values.getValue("id"), values.getValue("name"),
                    values.getValue("category"), values.getValue("location"))
                it.addEquipment(item)
                "Added ${item.equipmentId}."
            }
            Action.LIST -> store.read { catalog(it.listEquipment()) }
            Action.SHOW -> store.read { format(it.showEquipment(values.getValue("id"))) }
            Action.SEARCH -> store.read { catalog(it.searchEquipment(values.getValue("query"))) }
            Action.DELETE -> store.update {
                val id = values.getValue("id")
                it.deleteEquipment(id)
                "Deleted $id."
            }
            Action.CREATE -> store.update { format(it.createBooking(requireNotNull(command.slot))) }
            Action.BOOKINGS -> store.read {
                it.listBookings(values["equipment"], command.status).joinToString("\n", transform = ::format)
                    .ifEmpty { "No bookings." }
            }
            Action.CANCEL -> store.update { format(it.cancelBooking(values.getValue("id"))) }
        }
    }

    private fun catalog(items: List<Equipment>) =
        items.joinToString("\n", transform = ::format).ifEmpty { "No equipment." }

    private fun format(item: Equipment) =
        listOf(item.equipmentId, item.name, item.category, item.location).joinToString(" | ")

    private fun format(booking: Booking) = with(booking.slot) {
        listOf(booking.bookingId, equipmentId, requester, startTime, endTime, purpose, booking.status)
            .joinToString(" | ")
    }

    private fun help(): String = """
        |Usage: lab-booking [--data-file PATH] COMMAND [OPTIONS]
        |equipment add --id ID --name NAME --category CATEGORY --location LOCATION
        |equipment list
        |equipment show --id ID
        |equipment search --query TEXT
        |equipment delete --id ID
        |booking create --equipment ID --requester NAME --start TIME --end TIME --purpose TEXT
        |booking list [--equipment ID] [--status ACTIVE|CANCELLED]
        |booking cancel --id BOOKING_ID
        |help | --help
        |Use ISO local times such as 2026-12-01T09:00.
    """.trimMargin()
}
