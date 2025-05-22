package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Event;
import es.ucm.fdi.iw.model.Friendship.FriendshipStatus;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.repository.UserRepository;
import es.ucm.fdi.iw.repository.MessageRepository;

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
@RequestMapping("user")
public class UserController {

	private static final Logger log = LogManager.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LocalData localData;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MessageRepository messageRepository;

	@ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        User u = (User) session.getAttribute("u");
        model.addAttribute("u", u);
        for (String name : new String[] { "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
        // Añade el contador de mensajes no leídos si el usuario está logueado
        if (u != null) {
            long unreadCount = messageRepository.countUnreadMessages(u.getId());
            model.addAttribute("unreadCount", unreadCount);
        } else {
            model.addAttribute("unreadCount", 0);
        }
    }

	@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "No eres administrador, y éste no es tu perfil")
	public static class NoEsTuPerfilException extends RuntimeException {
	}

	public String encodePassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@GetMapping("/{id}")
	public String getUser(@PathVariable long id, Model model, HttpSession session) {
		User currentUser = (User) session.getAttribute("u");
		User targetUser = entityManager.find(User.class, id);

		// Check if users are friends using JPQL
		boolean areFriends = entityManager.createQuery(
				"SELECT COUNT(f) > 0 FROM Friendship f " +
						"WHERE ((f.user1.id = :currentUserId AND f.user2.id = :targetUserId) OR " +
						"(f.user1.id = :targetUserId AND f.user2.id = :currentUserId)) " +
						"AND f.status = :status",
				Boolean.class)
				.setParameter("currentUserId", currentUser.getId())
				.setParameter("targetUserId", id)
				.setParameter("status", FriendshipStatus.ACCEPTED)
				.getSingleResult();

		model.addAttribute("user", targetUser);
		model.addAttribute("u", currentUser);
		model.addAttribute("areFriends", areFriends);

		log.info("Friendship status between {} and {}: {}",
				currentUser.getUsername(), targetUser.getUsername(), areFriends);
		
		List<Event> lEventos = entityManager
				.createQuery("SELECT p.event FROM Participation p WHERE p.user = :user AND p.enabled = true",
						Event.class)
				.setParameter("user", targetUser).getResultList();
		model.addAttribute("lEventos", lEventos);

		return "user";
	}


	@PostMapping("/search")
	public String search(@RequestParam(value = "username") String username, Model model, HttpSession session) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found " + username));
		model.addAttribute("user", user);
		String ret = "redirect:/user/" + user.getId();
		return ret;
	}

	/**
	 * Returns the default profile pic
	 * 
	 * @return
	 */
	private static InputStream defaultPic() {
		return new BufferedInputStream(Objects.requireNonNull(
				UserController.class.getClassLoader().getResourceAsStream(
						"static/img/default-pic.jpg")));
	}

	/**
	 * Downloads a profile pic for a user id
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@GetMapping("{id}/pic")
	public StreamingResponseBody getPic(@PathVariable long id) throws IOException {
		File f = localData.getFile("user", "" + id + ".jpg");
		InputStream in = new BufferedInputStream(f.exists() ? new FileInputStream(f) : UserController.defaultPic());
		return os -> FileCopyUtils.copy(in, os);
	}

	@PostMapping("/{id}/pic")
	@ResponseBody
	@Transactional
	public ResponseEntity<String> postUserPic(
			@PathVariable long id,
			@RequestParam("photo") MultipartFile photo,
			HttpSession session) {

		User target = entityManager.find(User.class, id);
		User requester = (User) session.getAttribute("u");

		if (target == null || target.getId() != requester.getId()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("No tienes permiso para modificar este usuario");
		}

		try {
			File f = localData.getFile("user", id + ".jpg");
			try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
				stream.write(photo.getBytes());
				target.setImageUrl("/user/" + id + "/pic");
				entityManager.persist(target);
				log.info("Successfully uploaded photo for user {}", id);
				return ResponseEntity.ok("Foto actualizada con éxito");
			}
		} catch (Exception e) {
			log.warn("Error uploading photo for user {}: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al subir la foto: " + e.getMessage());
		}
	}

	@PostMapping("/{id}/update")
	@ResponseBody
	@Transactional
	public ResponseEntity<String> updateUser(
			@PathVariable long id,
			@RequestBody UserUpdateDTO updateData,
			HttpSession session) {

		try {
			User requester = (User) session.getAttribute("u");
			User target = entityManager.find(User.class, id);

			if (target == null || target.getId() != requester.getId()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("No tienes permiso para modificar este usuario");
			}

			target.setUsername(updateData.getUsername());
			target.setFirstName(updateData.getFirstName());
			target.setEmail(updateData.getEmail());
			target.setPhonenumber(updateData.getPhonenumber());

			entityManager.merge(target);
			entityManager.flush();

			// Update session
			session.setAttribute("u", target);

			return ResponseEntity.ok("Usuario actualizado correctamente");
		} catch (Exception e) {
			log.error("Error updating user: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al actualizar el usuario: " + e.getMessage());
		}
	}

	@Data
	public static class UserUpdateDTO {
		private String username;
		private String firstName;
		private String email;
		private String phonenumber;
	}

}