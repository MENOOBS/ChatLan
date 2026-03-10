package chatlan.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        CHAT, JOIN, LEAVE, SYSTEM, PRIVATE, USER_LIST
    }

    private String sender;
    private String content;
    private Type type;
    private String timestamp;
    private String recipient; // for private messages

    public Message(String sender, String content, Type type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Message(String sender, String content, Type type, String recipient) {
        this(sender, content, type);
        this.recipient = recipient;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public Type getType() { return type; }
    public String getTimestamp() { return timestamp; }
    public String getRecipient() { return recipient; }

    public void setSender(String sender) { this.sender = sender; }
    public void setContent(String content) { this.content = content; }
    public void setType(Type type) { this.type = type; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
}
