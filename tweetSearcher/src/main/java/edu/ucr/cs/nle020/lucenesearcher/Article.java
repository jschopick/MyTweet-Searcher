package edu.ucr.cs.nle020.lucenesearcher;

public class Article {

	 public String rank;
	 public String text;
	 public String username;
	 public String location;
	 public String link;
	 public String time;

    public Article(){}

    public Article(String rank, String text, String username, String location, String link, String time) {
        this.rank = rank;
    	this.text = text;
        this.username = username;
        this.location = location;
        this.link = link;
        this.time = time;
    }

    public String getScore() {
        return rank;
    }

    public void setScore(String rank) {
        this.rank = rank;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("Article[rank=%d, text=%s, username=%s, location=%s, link=%s, time=%s]", rank, text, username, location, link, time);
    }
}

