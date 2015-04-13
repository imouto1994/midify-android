package sg.edu.nus.POJOs;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/* MIDI POJO */
public class MidiPOJO {
    private static final String UNDEFINED = "undefined";

    @SerializedName("_id")
    private String fileId;

    @SerializedName("refId")
    private String refId;

    @SerializedName("ownerId")
    private String ownerId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("title")
    private String fileName;

    @SerializedName("filePath")
    private String serverFilePath;

    private String localFilePath;

    @SerializedName("isPublic")
    private boolean isPublic;

    @SerializedName("editedTime")
    private Date editedTime;

    public static MidiPOJO createLocalMidi(String fileName, String filePath, String fileId, String userId) {
        MidiPOJO instance = new MidiPOJO();
        instance.fileName = fileName;
        instance.localFilePath = filePath;
        instance.fileId = fileId;
        instance.ownerId = userId;
        instance.userId = userId;
        instance.editedTime = new Date();

        return instance;
    }

    public static MidiPOJO createLocalMidiWithoutId(String fileName, String filePath, String userId) {
        return createLocalMidi(fileName, filePath, UNDEFINED, userId);
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getLocalFilePath() {
        return this.localFilePath;
    }

    public String getServerFilePath() {
        return this.serverFilePath;
    }

    public String getFileId() {
        return this.fileId;
    }

    public String getRefId() {
        return this.refId;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public String getUserId() {
        return this.userId;
    }

    public boolean getIsPublic() {
        return this.isPublic;
    }

    public Date getEditedTime() {
        return this.editedTime;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLocalFilePath(String filePath) {
        this.localFilePath = filePath;
    }

    public void setServerFilePath(String filePath) {
        this.serverFilePath = filePath;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public void setOwnerId(String userId) {
        this.ownerId = userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setEditedTime(Date time) {
        this.editedTime = time;
    }
}
