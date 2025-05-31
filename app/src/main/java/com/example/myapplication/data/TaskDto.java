package com.example.myapplication.data;

/** Model pojedynczego zadania (backend DTO). */
public class TaskDto {

    private Long id;
    private String title;
    private String assigneeFullName;
    private boolean done;

    /* ───  Konstruktory  ─── */
    public TaskDto() {}                                   // wymagany przez GSON

    public TaskDto(Long id, String title,
                   String assigneeFullName, boolean done) {
        this.id = id;
        this.title = title;
        this.assigneeFullName = assigneeFullName;
        this.done = done;
    }

    /* ───  get  ─── */
    public Long    getId()               { return id; }
    public String  getTitle()            { return title; }
    public String  getAssigneeFullName() { return assigneeFullName; }
    public boolean isDone()              { return done; }

    /* ───  set  ─── */
    public void setId(Long id)                         { this.id = id; }
    public void setTitle(String title)                 { this.title = title; }
    public void setAssigneeFullName(String fullName)   { this.assigneeFullName = fullName; }
    public void setDone(boolean done)                  { this.done = done; }
}
