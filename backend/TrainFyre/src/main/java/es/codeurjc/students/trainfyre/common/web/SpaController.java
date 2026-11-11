package es.codeurjc.students.trainfyre.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Entrega la SPA para las rutas de navegación que no son recursos estáticos.
 * React decide después si la ruta existe y muestra su página 404.
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
