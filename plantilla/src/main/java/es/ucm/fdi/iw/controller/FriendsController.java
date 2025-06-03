package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Friendship;
import es.ucm.fdi.iw.model.Friendship.FriendshipStatus;
import es.ucm.fdi.iw.repository.MessageRepository;
import es.ucm.fdi.iw.model.User;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager; // Change this import
import org.apache.logging.log4j.Logger; // Change this import

import java.sql.Timestamp;
import java.util.List;

@Controller
@RequestMapping("/friends")
public class FriendsController {
    private static final Logger log = LogManager.getLogger(FriendsController.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

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

    @GetMapping("")
    public String getFriendsPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");

        if (user == null) {
            return "redirect:/login";
        } else {
            // Obtener todas las conversaciones con el conteo de mensajes no leídos
            List<Object[]> conversations = messageRepository.findAllConversationsWithUnreadCount(user.getId());

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

        try {
            // Get user's friends (accepted friendships only)
            List<User> friends = entityManager.createQuery(
                    "SELECT DISTINCT u FROM User u WHERE u IN " +
                            "(SELECT f.user2 FROM Friendship f WHERE f.user1 = :user AND f.status = :status) OR " +
                            "u IN (SELECT f.user1 FROM Friendship f WHERE f.user2 = :user AND f.status = :status)",
                    User.class)
                    .setParameter("user", user)
                    .setParameter("status", FriendshipStatus.ACCEPTED)
                    .getResultList();

            // Get pending friend requests
            List<Friendship> pendingRequests = entityManager.createQuery(
                    "SELECT f FROM Friendship f WHERE f.user2 = :user AND f.status = :status",
                    Friendship.class)
                    .setParameter("user", user)
                    .setParameter("status", FriendshipStatus.PENDING)
                    .getResultList();

            // Get suggestions (users who are not friends and no pending requests)
            List<User> suggestions = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u != :user " +
                            "AND u NOT IN (SELECT f.user2 FROM Friendship f WHERE f.user1 = :user) " +
                            "AND u NOT IN (SELECT f.user1 FROM Friendship f WHERE f.user2 = :user)",
                    User.class)
                    .setParameter("user", user)
                    .getResultList();

            model.addAttribute("friends", friends);
            model.addAttribute("pendingRequests", pendingRequests);
            model.addAttribute("suggestions", suggestions);

            log.info("Loaded friends page for user {}: {} friends, {} pending requests, {} suggestions",
                    user.getUsername(), friends.size(), pendingRequests.size(), suggestions.size());

            return "friends";

        } catch (Exception e) {
            log.error("Error loading friends page: {}", e.getMessage(), e);
            return "error";
        }
    }

    @PostMapping("/request/{id}")
    @Transactional
    public String sendFriendRequest(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("u");
        User targetUser = entityManager.find(User.class, id);

        if (currentUser == null || targetUser == null) {
            log.warn("Friend request failed: invalid users");
            return "redirect:/friends";
        }

        try {
            // Check if request already exists
            Long count = entityManager.createQuery(
                    "SELECT COUNT(f) FROM Friendship f WHERE f.user1 = :user1 AND f.user2 = :user2",
                    Long.class)
                    .setParameter("user1", currentUser)
                    .setParameter("user2", targetUser)
                    .getSingleResult();

            if (count == 0) {
                Friendship friendship = new Friendship();
                friendship.setUser1(currentUser);
                friendship.setUser2(targetUser);
                friendship.setStatus(FriendshipStatus.PENDING);
                friendship.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                entityManager.persist(friendship);
                entityManager.flush();

                log.info("Friend request sent from {} to {}",
                        currentUser.getUsername(), targetUser.getUsername());
            }
        } catch (Exception e) {
            log.error("Error sending friend request: {}", e.getMessage(), e);
            throw e;
        }

        return "redirect:/friends";
    }

    @PostMapping("/accept/{id}")
    @Transactional
    public String acceptFriendRequest(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("u");
        User requester = entityManager.find(User.class, id);

        if (currentUser == null || requester == null) {
            log.warn("Accept friendship failed: invalid users");
            return "redirect:/friends";
        }

        try {
            // Find and update the original friendship request
            Friendship originalFriendship = entityManager.createQuery(
                    "SELECT f FROM Friendship f WHERE f.user1 = :requester AND f.user2 = :user AND f.status = :status",
                    Friendship.class)
                    .setParameter("requester", requester)
                    .setParameter("user", currentUser)
                    .setParameter("status", FriendshipStatus.PENDING)
                    .getSingleResult();

            Timestamp now = new Timestamp(System.currentTimeMillis());

            // Update original friendship
            originalFriendship.setStatus(FriendshipStatus.ACCEPTED);
            originalFriendship.setIsAccepted(true);
            originalFriendship.setAcceptedAt(now);
            entityManager.merge(originalFriendship);

            // Create or update reverse friendship
            Friendship reverseFriendship = entityManager.createQuery(
                    "SELECT f FROM Friendship f WHERE f.user1 = :user1 AND f.user2 = :user2",
                    Friendship.class)
                    .setParameter("user1", currentUser)
                    .setParameter("user2", requester)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {
                        Friendship newFriendship = new Friendship();
                        newFriendship.setUser1(currentUser);
                        newFriendship.setUser2(requester);
                        newFriendship.setCreatedAt(now);
                        entityManager.persist(newFriendship);
                        return newFriendship;
                    });

            // Update reverse friendship
            reverseFriendship.setStatus(FriendshipStatus.ACCEPTED);
            reverseFriendship.setIsAccepted(true);
            reverseFriendship.setAcceptedAt(now);
            entityManager.merge(reverseFriendship);

            // Ensure changes are written
            entityManager.flush();

            log.info("Bidirectional friendship established between {} and {}",
                    currentUser.getUsername(), requester.getUsername());

        } catch (Exception e) {
            log.error("Error accepting friendship: {}", e.getMessage(), e);
            throw e;
        }

        return "redirect:/friends";
    }

    @PostMapping("/decline/{id}")
    @Transactional
    public String declineFriendRequest(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("u");
        User requester = entityManager.find(User.class, id);

        Friendship friendship = entityManager.createQuery(
                "SELECT f FROM Friendship f WHERE f.user1 = :requester AND f.user2 = :user AND f.status = :status",
                Friendship.class)
                .setParameter("requester", requester)
                .setParameter("user", currentUser)
                .setParameter("status", FriendshipStatus.PENDING)
                .getSingleResult();

        if (friendship != null) {
            friendship.setStatus(FriendshipStatus.DECLINED);
            friendship.setIsAccepted(false);
        }

        return "redirect:/friends";
    }
}
