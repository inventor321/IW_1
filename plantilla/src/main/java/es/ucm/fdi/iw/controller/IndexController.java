package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

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