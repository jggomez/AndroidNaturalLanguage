package co.devhack.naturallanguage.models;

/**
 * Created by jggomez on 03-Mar-17.
 */

public class EntityInfo {

    private String name;
    private String type;
    private float salience;
    private String wikipediaURL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getSalience() {
        return salience;
    }

    public void setSalience(float salience) {
        this.salience = salience;
    }

    public String getWikipediaURL() {
        return wikipediaURL;
    }

    public void setWikipediaURL(String wikipediaURL) {
        this.wikipediaURL = wikipediaURL;
    }
}
