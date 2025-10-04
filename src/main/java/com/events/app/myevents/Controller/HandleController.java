package com.events.app.myevents.Controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class HandleController implements ErrorController {

    @GetMapping
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
       
        Integer statusCode = Integer.valueOf(status.toString());
         HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        if (status != null) {

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("message", httpStatus.getReasonPhrase());
                return "error/error-404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("message", httpStatus.getReasonPhrase());
                return "error/error-500";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("message", httpStatus.getReasonPhrase());
                return "error/error-403";
            }
        }
        model.addAttribute("message", httpStatus.getReasonPhrase());
        return "error/error";
    }
}
