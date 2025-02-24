package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * A message that users can send to a group.
 *
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "GroupMessage.countUnread", query = "SELECT COUNT(m) FROM GroupMessage m "
                + "WHERE m.recipient.id = :userId AND m.dateRead = null")
})
@Data
public class GroupMessage implements Transferable<GroupMessage.Transfer> {

    private static Logger log = LogManager.getLogger(GroupMessage.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;
    @ManyToOne
    private User sender;

    private Event recipient;
    private String text;

    private LocalDateTime dateSent;
    private LocalDateTime dateRead;

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
        long id;

        public Transfer(GroupMessage m) {
            this.from = m.getSender().getUsername();
            this.to = m.getRecipient().getName();
            this.sent = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(m.getDateSent());
            this.received = m.getDateRead() == null ? null
                    : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(m.getDateRead());
            this.text = m.getText();
            this.id = m.getId();
        }
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(sender.getUsername(), recipient.getName(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateSent),
                dateRead == null ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateRead),
                text, id);
    }
}