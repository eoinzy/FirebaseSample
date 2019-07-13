package com.example.firebasesample.model;

import com.google.firebase.database.PropertyName;

public class Report {

    private String email;
    private String profile_image;
    private Question question_one;
    private Question question_two;
    private String time_stamp;

    public Report() {}

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_image() {
        return profile_image;
    }

    @PropertyName("profile_image")
    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    @PropertyName("question_one")
    public Question getQuestion_one() {
        return question_one;
    }

    public void setQuestion_one(Question question_one) {
        this.question_one = question_one;
    }

    @PropertyName("question_two")
    public Question getQuestion_two() {
        return question_two;
    }

    public void setQuestion_two(Question question_two) {
        this.question_two = question_two;
    }

    @PropertyName("time_stamp")
    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }
}