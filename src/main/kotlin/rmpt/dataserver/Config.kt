package rmpt.dataserver

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.Optional
import javax.annotation.PostConstruct

@Configuration
class Config : WebMvcConfigurer {

    @Value("\${endpoints:#{null}}")
    private val endpointsParam: Optional<String>? = null

    @Value("\${autoGenerateIds:#{true}}")
    private var autoGenerateIds: Boolean = true

    @Value("\${usePagination:#{false}}")
    private var usePagination: Boolean = false

    @PostConstruct
    private fun outputConfiguration() {
        var endpointsPrintable: String
        var endpointsParts: List<String>
        if (endpointsParam != null && endpointsParam.isPresent) {
            endpointsPrintable = endpointsParam.get()
            endpointsParts = endpointsPrintable.split(",")
        } else {
            endpointsPrintable = ""
            endpointsParts = listOf<String>()
        }

        println("[usePagination=$usePagination] Pagination is ${if (usePagination) "enabled" else "disabled"}")
        println("[autoGenerateIds=$autoGenerateIds] Automatic id generation is ${if (autoGenerateIds) "enabled" else "disabled"}")
        println("[endpoints=$endpointsPrintable] ${endpointsParts.size} endpoints will be created")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    }
}
