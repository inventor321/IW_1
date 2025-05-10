   package es.ucm.fdi.iw.controller;

    import es.ucm.fdi.iw.LocalData;
    import es.ucm.fdi.iw.model.Event;
    import es.ucm.fdi.iw.model.Friendship.FriendshipStatus;
    import es.ucm.fdi.iw.model.Message;
    import es.ucm.fdi.iw.model.User;
    import es.ucm.fdi.iw.model.User.Role;
    import es.ucm.fdi.iw.repository.UserRepository;
    
    import org.apache.logging.log4j.LogManager;
    import org.apache.logging.log4j.Logger;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.util.FileCopyUtils;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import org.springframework.web.server.ResponseStatusException;
    import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
    
    import jakarta.persistence.EntityManager;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import jakarta.servlet.http.HttpSession;
    import jakarta.transaction.Transactional;
    import lombok.Data;
    
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    
    import java.io.*;
    import java.security.SecureRandom;
    import java.time.LocalDateTime;
    import java.util.Base64;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Objects;
    import java.util.stream.Collectors;
    
    @Controller
    @RequestMapping("index")
    public class IndexController {
    
        @Autowired
        private UserRepository userRepository;

        @PostMapping("/search")
	    public String search(@RequestParam(value="username") String username, Model model, HttpSession session) {
		    User user = userRepository.findByUsername(username).orElse(null);
            model.addAttribute("user", user);
            String ret = "";
            if(user == null){
                ret = "redirect:/";
            }else{
                ret = "redirect:/user/" + user.getId();
            }
		
		    return ret;
	    }

    }