package Web.Services

import Web.DTO.DesignColorsEndpointDTO
import io.ktor.server.application.ApplicationEnvironment

class DesignSettingsService(private val environment: ApplicationEnvironment) {
    fun getColors(): DesignColorsEndpointDTO = DesignColorsEndpointDTO(
        primary = color("primary", "#55759f"),
        secondary = color("secondary", "#e7eef6"),
        warning = color("warning", "#a86f1d"),
        error = color("error", "#c64646"),
        success = color("success", "#17795f")
    )

    private fun color(name: String, default: String): String {
        val value = environment.config.propertyOrNull("design.colors.$name")?.getString()?.trim()
        return value?.takeIf(::isHexColor) ?: default
    }

    private fun isHexColor(value: String): Boolean = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$").matches(value)
}
