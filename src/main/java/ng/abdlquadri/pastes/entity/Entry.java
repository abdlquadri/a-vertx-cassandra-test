package ng.abdlquadri.pastes.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * Created by abdlquadri on 10/9/16.
 * <p>
 * Entry Data Object
 */

@DataObject(generateConverter = true)
public class Entry {

    private UUID id;
    private String body;
    private String title;
    private long expires;//expiry timestamp
    private long creationDate;
    private boolean visible;//private is keyword
    private String secret;

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getSecret() {
        return secret;

    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Entry() {
    }

    public Entry(Entry entry) {

        this.id = entry.id;
        this.body = entry.body;
        this.title = entry.title;
        this.expires = entry.expires;
        this.visible = entry.visible;
        this.secret = entry.secret;
        this.creationDate = entry.creationDate;
    }

    public Entry(JsonObject pasteJson) {
        EntryConverter.fromJson(pasteJson, this);
    }

    public Entry(String pasteString) {
        EntryConverter.fromJson(new JsonObject(pasteString), this);
    }

    public Entry(UUID id, String body, String title, long expires, boolean visible, String secret, long creationDate) {
        this.id = id;
        this.body = body;
        this.title = title;
        this.expires = expires;
        this.visible = visible;
        this.secret = secret;
        this.creationDate = creationDate;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        EntryConverter.toJson(this, jsonObject);
        return jsonObject;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
