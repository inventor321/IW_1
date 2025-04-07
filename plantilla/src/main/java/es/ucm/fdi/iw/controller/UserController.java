package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Event;
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

	@ModelAttribute
	public void populateModel(HttpSession session, Model model) {
		for (String name : new String[] { "u", "url", "ws" }) {
			model.addAttribute(name, session.getAttribute(name));
		}
	}

	@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "No eres administrador, y éste no es tu perfil")
	public static class NoEsTuPerfilException extends RuntimeException {
	}

	public String encodePassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@GetMapping("/{id}")
	public String index(@PathVariable long id, Model model, HttpSession session) {
		User target = userRepository.findById(id).orElse(null);
		model.addAttribute("user", target);
		return "user";
	}

	@PostMapping("/search")
	@ResponseBody
	public String search(@RequestParam String username, Model model, HttpSession session) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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

	@PostMapping("{id}/pic")
	@ResponseBody
	public String setPic(@RequestParam("photo") MultipartFile photo, @PathVariable long id,
			HttpServletResponse response, HttpSession session, Model model) throws IOException {

		User target = entityManager.find(User.class, id);
		model.addAttribute("user", target);

		// check permissions
		User requester = (User) session.getAttribute("u");
		if (requester.getId() != target.getId() &&
				!requester.hasRole(Role.ADMIN)) {
			throw new NoEsTuPerfilException();
		}

		log.info("Updating photo for user {}", id);
		File f = localData.getFile("user", "" + id + ".jpg");
		if (photo.isEmpty()) {
			log.info("failed to upload photo: empty file?");
		} else {
			try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
				byte[] bytes = photo.getBytes();
				stream.write(bytes);
				log.info("Uploaded photo for {} into {}!", id, f.getAbsolutePath());
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log.warn("Error uploading " + id + " ", e);
			}
		}
		return "{\"status\":\"photo uploaded correctly\"}";
	}

	@PostMapping("/{id}/update")
	@ResponseBody
	@Transactional
	public ResponseEntity<String> updateUser(
			@PathVariable long id, 
			@RequestBody UserUpdateDTO updateData,
			HttpSession session) {
		
		User requester = (User)session.getAttribute("u");
		User target = entityManager.find(User.class, id);
		
		// Check if user exists and requester has permission
		if (target == null || target.getId() != requester.getId()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para actualizar este usuario");
		}

		try {
			// Check if username is already taken by another user
			Long existingCount = entityManager.createQuery(
				"SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.id != :userId", 
				Long.class)
				.setParameter("username", updateData.getUsername())
				.setParameter("userId", id)
				.getSingleResult();

			if (existingCount > 0) {
				return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso");
			}

			// Update user data
			target.setUsername(updateData.getUsername());
			target.setEmail(updateData.getEmail());
			target.setFirstName(updateData.getFname());
			target.setPhonenumber(updateData.getPnumber());
			entityManager.flush();

			return ResponseEntity.ok("Usuario actualizado correctamente");
		} catch (Exception e) {
			log.error("Error updating user: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error al actualizar el usuario");
		}
	}

	// DTO class for user updates
	@Data
	private static class UserUpdateDTO {
		private String username;
		private String email;
		private String fname;
		private String pnumber;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getFname() {
			return fname;
		}

		public void setFname(String fname) {
			this.fname = fname;
		}

		public String getPnumber() {
			return pnumber;
		}

		public void setPnumber(String pnumber) {
			this.pnumber = pnumber;
		}
	}

}