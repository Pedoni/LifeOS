package it.edon.lifeos.feature.tasks

import it.edon.lifeos.core.domain.model.DueDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** What the person picked: a day, and optionally a time of day in their own time zone. */
internal data class DueSelection(val date: LocalDate, val time: LocalTime?)

/** A day alone is due all day; a day with a time is an exact moment in [zone]. */
internal fun DueSelection?.toDueDate(zone: ZoneId): DueDate? = when {
    this == null -> null
    time == null -> DueDate.AllDay(date)
    else -> DueDate.At(date.atTime(time).atZone(zone).toInstant())
}

internal fun DueDate.toSelection(zone: ZoneId): DueSelection = when (this) {
    is DueDate.AllDay -> DueSelection(date, null)

    is DueDate.At -> {
        val local = instant.atZone(zone).toLocalDateTime()
        DueSelection(local.toLocalDate(), local.toLocalTime().withSecond(0).withNano(0))
    }
}

internal fun DueDate.format(zone: ZoneId, locale: Locale): String = when (this) {
    is DueDate.AllDay -> date.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    )

    is DueDate.At -> instant.atZone(zone).format(
        DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.MEDIUM,
            FormatStyle.SHORT
        ).withLocale(locale)
    )
}
