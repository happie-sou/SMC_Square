package com.example.smc_square;

public class NewPost {
    private String description;
    private String proPic;
    private String splashPic;
    private String username;
    private String depNo;
    private String splashStorageReference;
    private int reactionCount;
    private int commentCount;

    public NewPost() {
        //empty constructor needed for firebase
    }

    public NewPost(String desc, String proPic, String splashPic, String username, String depNo, String splashStorageReference) {
        description = desc;
        this.proPic = proPic;
        this.splashPic = splashPic;
        this.username = username;
        this.depNo = depNo;
        this.splashStorageReference = splashStorageReference;
        this.reactionCount = 0;
        this.commentCount = 0;
    }

    public NewPost(String desc, String proPic, String username, String depNo){
        description = desc;
        this.proPic = proPic;
        this.username = username;
        this.depNo = depNo;
        this.reactionCount = 0;
        this.commentCount = 0;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(int reactionCount) {
        this.reactionCount = reactionCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProPic() {
        return proPic;
    }

    public String getDepNo() {
        return depNo;
    }

    public void setDepNo(String depNo) {
        this.depNo = depNo;
    }

    public String getSplashStorageReference() {
        return splashStorageReference;
    }

    public void setSplashStorageReference(String splashStorageReference) {
        this.splashStorageReference = splashStorageReference;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    public String getSplashPic() {
        return splashPic;
    }

    public void setSplashPic(String splashPic) {
        this.splashPic = splashPic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
