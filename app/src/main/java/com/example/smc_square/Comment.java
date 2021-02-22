package com.example.smc_square;

public class Comment {
    private String comment,depNo,proPic,name;

    public Comment(){
        //empty constructor needed for firebase
    }

    public Comment(String comment,String depNo,String proPic,String name){
        this.comment=comment;
        this.depNo = depNo;
        this.proPic = proPic;
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDepNo() {
        return depNo;
    }

    public void setDepNo(String depNo) {
        this.depNo = depNo;
    }

    public String getProPic() {
        return proPic;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
