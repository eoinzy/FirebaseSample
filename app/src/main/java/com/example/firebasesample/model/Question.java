package com.example.firebasesample.model;

import com.google.firebase.database.PropertyName;

public class Question {
    @PropertyName("answer")
    private String answer;
    @PropertyName("question")
    private String question;
    @PropertyName("reason")
    private String reason;

    public Question() { }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
