package es.ucm.fdi.iw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Non-authenticated requests only.
 */
@Controller
public class RootController {

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        boolean error = request.getQueryString() != null && request.getQueryString().indexOf("error") != -1;
        model.addAttribute("loginError", error);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model, HttpServletRequest request) {
        boolean error = request.getQueryString() != null && request.getQueryString().indexOf("error") != -1;
        model.addAttribute("RegisterError", error);
        return "register";
    }

    @GetMapping("/event")
    public String event(Model model) {
        return "event";
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }
}
