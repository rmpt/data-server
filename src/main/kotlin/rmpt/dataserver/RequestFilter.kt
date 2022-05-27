package rmpt.dataserver

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Locale
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RequestFilter(
    private val dataService: DataService
) : OncePerRequestFilter() {

    private fun validRequest(requestUri: String, isPost: Boolean): Boolean {

        if (requestUri == "/") return true

        val trimmedRequestUri = requestUri.substring(1, requestUri.length)

        if (dataService.exists(trimmedRequestUri)) return true

        if (isPost) return false // for post requests we need to call the clean endpoint

        val lastSlashIndex = trimmedRequestUri.lastIndexOf("/")
        val endpoint = if (lastSlashIndex == -1) "ROOT" else trimmedRequestUri.substring(0, lastSlashIndex)
        return dataService.exists(endpoint)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!validRequest(request.requestURI, "post" == request.method.lowercase())) {
            println("Invalid request '${request.requestURI}'.")
            response.status = HttpStatus.BAD_REQUEST.value()
            return
        }

        filterChain.doFilter(request, response)
    }
}
