package com.example.demo.todolist.form;

import com.example.demo.todolist.entity.Todo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
@Data
public class TodoData {
	private Integer id;
	
	@NotBlank(message = "件名を入力してください")
	private String title;
	
	@NotNull(message = "重要度を選択してください")
	private Integer importance;
	
	@Min(value = 0, message = "緊急度を選択してください")
	private Integer urgency;
	
	private String deadline;
	private String done;
	
	/** 入力データからエンティティを生成して返す */
	
	public Todo toEntity() {
		Todo todo = new Todo();
		todo.setId(id);
		todo.setTitle(title);
		todo.setImportance(importance);
		todo.setUrgency(urgency);
		todo.setDone(done);
		
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		long ms;
		try {
			ms = sdFormat.parse(deadline).getTime();
			todo.setDeadline(new Date(ms));
		} catch (ParseException e) {
			todo.setDeadline(null);
		}
		return todo;
	}
}
