package com.example.demo.todolist.form;

import lombok.Data;

@Data
public class TodoQuery {
	private String title;
	private Integer importance;
	private Integer urgency;
	private String deadlineFrom;
	private String deadlineTo;
	private String done;
	private Integer currentPage = 0;
	public TodoQuery() {
		title = "";
		importance = -1;
		urgency= -1;
		deadlineFrom="";
		deadlineTo = "";
		done = "";
		currentPage = 0;
	}
}
