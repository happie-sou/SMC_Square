package com.example.smc_square;

import java.util.Map;

public class Reaction {
    private String name, proPic;
    private Map<String, Object> reaction;

    public Reaction(){
        //empty constructor needed for firebase
    }

    public Reaction(String name, String proPic, Map<String, Object> reaction) {
        this.name = name;
        this.proPic = proPic;
        this.reaction = reaction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProPic() {
        return proPic;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    public Map<String, Object> getReaction() {
        return reaction;
    }

    public void setReaction(Map<String, Object> reaction) {
        this.reaction = reaction;
    }
}
