package com.example.myapplication.data;
public class CreateTaskDto {
    private String title, assigneeFullName;
    private Long eventId, creatorId;
    public CreateTaskDto(String t,String a,Long e,Long c){
        title=t; assigneeFullName=a; eventId=e; creatorId=c;}

}