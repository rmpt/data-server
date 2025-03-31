package rmpt.dataserver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class DataController(private val dataService: DataService) {

    @Value("\${ignoreQueryParams:#{null}}")
    private val ignoreQueryParams: String? = null

    @Value("\${usePagination:#{false}}")
    private var usePagination: Boolean = false

    private val mapper = ObjectMapper()

    private val ignoreQueryParamsSet: HashSet<String> = HashSet()

    @PostConstruct
    private fun init() {
        if (ignoreQueryParams != null) {
            ignoreQueryParamsSet.addAll(ignoreQueryParams.split(","))
        }
    }

    private fun getEndpoint(requestUri: String): Pair<String, String?> {
        if (requestUri == "/") return "ROOT" to null

        val trimmedRequestUri = requestUri.substring(1, requestUri.length)

        if (dataService.exists(trimmedRequestUri)) return trimmedRequestUri to null

        val lastSlashIndex = trimmedRequestUri.lastIndexOf("/")
        val id = trimmedRequestUri.substring(lastSlashIndex + 1, trimmedRequestUri.length)
        val endpoint = if (lastSlashIndex == -1) "ROOT" else trimmedRequestUri.substring(0, lastSlashIndex)
        return endpoint to id
    }

    private fun requestParamMapToPairList(request: HttpServletRequest): List<Pair<String, String>> =
        request.parameterMap
            .filter { !ignoreQueryParamsSet.contains(it.key) }
            .map { it.key!! to it.value[0]!! }

    @PostMapping("**")
    @ResponseStatus(HttpStatus.CREATED)
    fun post(request: HttpServletRequest, @RequestBody content: String): JsonNode? {
        val (endpoint, _) = getEndpoint(request.requestURI)
        val jsonElement = mapper.readTree(content)
        dataService.add(endpoint, jsonElement)
        return jsonElement
    }

    @GetMapping("**")
    fun get(
        request: HttpServletRequest,
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "pageSize", defaultValue = "10") pageSize: Int
    ): Any? {
        val (endpoint, id) = getEndpoint(request.requestURI)
        val queryParams = requestParamMapToPairList(request)
        val pageable = PageRequest.of(page, pageSize)
        return when {
            id != null -> dataService.getById(endpoint, id)
            queryParams.isNotEmpty() ->
                if (usePagination) dataService.getPage(endpoint, queryParams, pageable)
                else dataService.get(endpoint, queryParams)
            else ->
                if (usePagination) dataService.getPage(endpoint, pageable)
                else dataService.getAll(endpoint)
        }
    }

    @PutMapping("**")
    fun put(request: HttpServletRequest, @RequestBody content: String): JsonNode {
        val (endpoint, id) = getEndpoint(request.requestURI)
        val jsonElement = mapper.readTree(content)
        return when {
            id != null -> dataService.updateById(endpoint, id, jsonElement)
            else -> dataService.update(endpoint, requestParamMapToPairList(request), jsonElement)
        }
    }

    @DeleteMapping("**")
    fun delete(request: HttpServletRequest): Boolean {
        val (endpoint, id) = getEndpoint(request.requestURI)
        return when {
            id != null -> dataService.deleteById(endpoint, id)
            else -> dataService.delete(endpoint, requestParamMapToPairList(request))
        }
    }
}
