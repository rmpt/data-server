package rmpt.dataserver

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RequestFilter(
    private val dataService: DataService
) : OncePerRequestFilter() {

    private fun getEndpoint(requestUri: String): String {
        return if(requestUri == "/") "ROOT" else requestUri.substring(1, requestUri.length)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val endpoint = getEndpoint(request.requestURI)
        if(!dataService.exists(endpoint)) {
            println("Endpoint '$endpoint' does not exist.")
            response.status = HttpStatus.BAD_REQUEST.value()
            return
        }

        filterChain.doFilter(request, response)
    }
}