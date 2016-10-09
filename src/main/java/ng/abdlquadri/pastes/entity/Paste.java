package ng.abdlquadri.pastes.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Created by abdlquadri on 10/9/16.
 * <p>
 * Paste Data Object
 */

@DataObject(generateConverter = true)
public class Paste {

    private int id;
    private String body;
    private String title;
    private long expires;//expiry timestamp
    private boolean visible;//private is keyword

    public Paste() {
    }

    public Paste(Paste paste) {

        this.id = paste.id;
        this.body = paste.body;
        this.title = paste.title;
        this.expires = paste.expires;
        this.visible = paste.visible;
    }

    public Paste(JsonObject pasteJson) {
        PasteConverter.fromJson(pasteJson, this);
    }

    public Paste(String pasteString) {
        PasteConverter.fromJson(new JsonObject(pasteString), this);
    }

    public Paste(int id, String body, String title, long expires, boolean visible) {
        this.id = id;
        this.body = body;
        this.title = title;
        this.expires = expires;
        this.visible = visible;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        PasteConverter.toJson(this, jsonObject);
        return jsonObject;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
