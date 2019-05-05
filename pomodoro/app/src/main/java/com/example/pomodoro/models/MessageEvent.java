package com.example.pomodoro.models;

public class MessageEvent {

    private final String time;
    private final int text;
    private final int percentage;


    public MessageEvent(String time, int text, int percentage) {
        this.time = time;
        this.text = text;
        this.percentage = percentage;
    }

    /**
     * Get the time
     * @return - the time
     */
    public String getTime(){
        return time;
    }

    /**
     * Get the int of the string
     * @return - the id of the string
     */
    public int getText(){
        return text;
    }

    /**
     * Get the percentage
     * @return - the percentage
     */
    public int getPercentage(){
        return percentage;
    }

}