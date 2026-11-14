package es.codeurjc.students.trainfyre.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Serves the SPA for the navigation routes that are not static resources.
 * React then decides whether the route exists and shows its 404 page.
 */
@Controller
public class SpaController {

    @RequestMapping({
            "/{path:[^\\.]*}",
            "/**/{path:[^\\.]*}"
    })
    public String forwardToSpa(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return "forward:/index.html";
    }
}
