package com.example.smc_square;

public class Friend {
    private String name, proPic, depNo;

    public Friend() {
        //empty constructor needed for firebase
    }

    public Friend(String name, String proPic, String depNo) {
        this.name = name;
        this.proPic = proPic;
        this.depNo = depNo;
    }

    public String getName() {
        return name;
    }

    public String getProPic() {
        return proPic;
    }

    public String getDepNo() {
        return depNo;
    }
}
