package it.edon.lifeos.feature.tasks

import it.edon.lifeos.core.domain.model.DueDate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DueSelectionTest {
    private val rome = ZoneId.of("Europe/Rome")
    private val date = LocalDate.parse("2026-03-12")

    @Test
    fun noSelectionMeansNoDueDate() {
        assertNull((null as DueSelection?).toDueDate(rome))
    }

    @Test
    fun aDayWithoutATimeIsDueAllDay() {
        assertEquals(DueDate.AllDay(date), DueSelection(date, null).toDueDate(rome))
    }

    @Test
    fun aDayWithATimeIsTheMomentInThatTimeZone() {
        val due = DueSelection(date, LocalTime.of(17, 30)).toDueDate(rome)

        assertEquals(DueDate.At(Instant.parse("2026-03-12T16:30:00Z")), due)
    }

    @Test
    fun anExactMomentReturnsToTheSameDayAndTimeLocally() {
        val selection = DueDate.At(Instant.parse("2026-03-12T16:30:45Z")).toSelection(rome)

        assertEquals(DueSelection(date, LocalTime.of(17, 30)), selection)
    }

    @Test
    fun theSameMomentIsAnotherDayInAnotherZone() {
        val selection = DueDate.At(
            Instant.parse("2026-03-12T23:30:00Z")
        ).toSelection(ZoneId.of("Asia/Tokyo"))

        assertEquals(LocalDate.parse("2026-03-13"), selection.date)
    }

    @Test
    fun anAllDayDateDoesNotMoveWithTheZone() {
        assertEquals(
            DueSelection(date, null),
            DueDate.AllDay(date).toSelection(ZoneId.of("Pacific/Auckland"))
        )
    }

    @Test
    fun formatsAccordingToTheLocale() {
        assertEquals("12 Mar 2026", DueDate.AllDay(date).format(rome, Locale.UK))
        assertEquals("Mar 12, 2026", DueDate.AllDay(date).format(rome, Locale.US))
    }
}
