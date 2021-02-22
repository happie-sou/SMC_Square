package com.example.smc_square;

public class Splash{
    public String username,splashPic,depNo,proPic,description;
    public int reactionCount, commentCount;
    public String comments[];

    public Splash() {
        //empty constructor needed for firebase
    }

    public Splash(String username, String splashPic,String proPic,String []comm,String depNo, String description, int reactionCount, int commentCount){
        this.depNo = depNo;
        this.username = username;
        this.splashPic = splashPic;
        this.proPic = proPic;
        this.comments = comm;
        this.description = description;
        this.reactionCount = reactionCount;
        this.commentCount = commentCount;
    }

    public String getDepNo() {
        return depNo;
    }

    public void setDepNo(String depNo) {
        this.depNo = depNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSplashPic() {
        return splashPic;
    }

    public void setSplashPic(String splashPic) {
        this.splashPic = splashPic;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getProPic() {
        return proPic;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(int reactionCount) {
        this.reactionCount = reactionCount;
    }

    public String[] getComments() {
        return comments;
    }

    public void setComments(String[] comments) {
        this.comments = comments;
    }
}
