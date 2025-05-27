package kcms.common

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.View
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionResolver : Loggable {
    @ExceptionHandler(Exception::class)
    fun doResolveException(
        request: HttpServletRequest,
        e: Exception
    ): View {
        return kcms.ui.ErrorPage(
            description = e.message,
            e = e
        )
    }
}
