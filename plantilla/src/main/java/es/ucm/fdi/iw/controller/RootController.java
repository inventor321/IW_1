package es.ucm.fdi.iw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import es.ucm.fdi.iw.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Non-authenticated requests only.
 */
@Controller
public class RootController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }

        User currentUser = (User) session.getAttribute("u");

        if (currentUser != null) {
            // Obtener todas las conversaciones con el conteo de mensajes no leídos
            List<Object[]> conversations = messageRepository.findAllConversationsWithUnreadCount(currentUser.getId());

            // Ordenar: primero las que tienen mensajes sin leer, luego las demás
            conversations.sort((a, b) -> {
                Long unreadA = (Long) a[1];
                Long unreadB = (Long) b[1];
                if ((unreadA > 0 && unreadB > 0) || (unreadA == 0 && unreadB == 0)) {
                    return 0;
                }
                return unreadA > 0 ? -1 : 1;
            });

            int unreadCount = conversations.stream()
                    .mapToInt(conversation -> ((Long) conversation[1]).intValue())
                    .sum();

            model.addAttribute("conversations", conversations);
            model.addAttribute("unreadCount", unreadCount);

        }

    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        boolean error = request.getQueryString() != null && request.getQueryString().indexOf("error") != -1;
        model.addAttribute("loginError", error);
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Asegúrate de que el archivo Thymeleaf se llama register.html
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @PostMapping("/register")
    @Transactional
    public String registerUser(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            Model model, HttpSession session) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya existe.");
            return "register";
        }
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "El correo electronico ya esta registrado.");
            return "register";
        }
        if (userRepository.findByPhonenumber(phone).isPresent()) {
            model.addAttribute("error", "El numero de telefono ya esta registrado.");
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(encodePassword(password));
        newUser.setFirstName(name);
        newUser.setEmail(email);
        newUser.setPhonenumber(phone);
        newUser.setEnabled(true);
        newUser.setRoles("USER");
        newUser.setLastlogin(LocalDateTime.now());
        userRepository.save(newUser);
        userRepository.flush();
        session.setAttribute("u", newUser);

        // Autenticar al usuario en Spring Security
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(newUser, null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return "redirect:/";
    }

    @GetMapping("/event")
    public String event(Model model) {
        return "event";
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        return "index";
    }

    @GetMapping("/chat")
    public String chat(Model model) {
        return "chat";
    }

}
