package com.example.smc_square;

import com.google.firebase.firestore.PropertyName;

public class sNotification {

    public String name,depNo,proPic,splashId,type,commentId;

    public sNotification(){

    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("commentId")
    public String getCommentId() {
        return commentId;
    }

    @PropertyName("commentId")
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    @PropertyName("depNo")
    public String getDepNo() {
        return depNo;
    }

    @PropertyName("depNo")
    public void setDepNo(String depNo) {
        this.depNo = depNo;
    }

    @PropertyName("proPic")
    public String getProPic() {
        return proPic;
    }

    @PropertyName("proPic")
    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    @PropertyName("splashId")
    public String getSplashId() {
        return splashId;
    }

    @PropertyName("splashId")
    public void setSplashId(String splashId) {
        this.splashId = splashId;
    }

    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }
}
