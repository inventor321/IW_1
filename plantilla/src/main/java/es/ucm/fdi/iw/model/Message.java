package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * A message that users can send each other.
 *
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "Message.countUnread", query = "SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.dateRead = null"),
		@NamedQuery(name = "Message.findEventMessages", query = "SELECT m FROM Message m WHERE m.event.id = :eventId ORDER BY m.dateSent"),
		@NamedQuery(name = "Message.findConversation", query = "SELECT m FROM Message m WHERE m.type = 'PRIVATE' AND ((m.sender.id = :user1Id AND m.recipient.id = :user2Id) OR (m.sender.id = :user2Id AND m.recipient.id = :user1Id)) ORDER BY m.dateSent")
})

@Data
public class Message implements Transferable<Message.Transfer> {

	private static Logger log = LogManager.getLogger(Message.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
	@SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;
	@ManyToOne
	private User sender;
	@ManyToOne
	private User recipient;
	private String text;
	private long usergroup;

	private LocalDateTime dateSent;
	private LocalDateTime dateRead;

	@ManyToOne
	private Event event; // Puede ser null para mensajes privados

	private MessageType type = MessageType.PRIVATE;

	public enum MessageType {
		PRIVATE, // Mensaje entre usuarios
		EVENT // Mensaje en un evento
	}

	/**
	 * Objeto para persistir a/de JSON
	 * 
	 * @author mfreire
	 */
	@Getter
	@AllArgsConstructor
	public static class Transfer {
		private String from;
		private String to;
		private String sent;
		private String received;
		private String text;
		private Long eventId; // Será null para mensajes privados
		private MessageType type;
		long id;

		public Transfer(Message m) {
			this.from = m.getSender().getUsername();
			this.to = m.getRecipient() != null ? m.getRecipient().getUsername() : null;
			this.sent = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(m.getDateSent());
			this.received = m.getDateRead() == null ? null
					: DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(m.getDateRead());
			this.text = m.getText();
			this.eventId = m.getEvent() != null ? m.getEvent().getId() : null;
			this.type = m.getType();
			this.id = m.getId();
		}
	}

	@Override
	public Transfer toTransfer() {
		return new Transfer(this);
	}
}
