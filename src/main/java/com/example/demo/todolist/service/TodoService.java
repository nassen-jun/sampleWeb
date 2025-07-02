package com.example.demo.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.demo.todolist.entity.Todo;
import com.example.demo.todolist.form.TodoData;
import com.example.demo.todolist.form.TodoQuery;
import com.example.demo.todolist.repository.TodoRepository;
import com.example.demo.todolist.common.Utils; // Utils が org.antlr のままだとおかしいので、正しいユーティリティクラスを使ってください

@Service
public class TodoService {
	private final TodoRepository todoRepository;

	public TodoService(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	public List<Todo> doQuery(TodoQuery todoQuery) {
		List<Todo> todoList = null;

		if (todoQuery.getTitle() != null && !todoQuery.getTitle().isEmpty()) {
			todoList = todoRepository.findByTitleLike("%" + todoQuery.getTitle() + "%");
		} else if (todoQuery.getImportance() != null && todoQuery.getImportance() != -1) {
			todoList = todoRepository.findByImportance(todoQuery.getImportance());
		} else if (todoQuery.getUrgency() != null && todoQuery.getUrgency() != -1) {
			todoList = todoRepository.findByUrgency(todoQuery.getUrgency());
		} else if (!todoQuery.getDeadlineFrom().isEmpty() && todoQuery.getDeadlineTo().isEmpty()) {
			todoList = todoRepository.findByDeadlineGreaterThanEqualOrderByDeadlineAsc(
				Utils.str2date(todoQuery.getDeadlineFrom()));
		} else if (todoQuery.getDeadlineFrom().isEmpty() && !todoQuery.getDeadlineTo().isEmpty()) {
			todoList = todoRepository.findByDeadlineLessThanEqualOrderByDeadlineAsc(
				Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (!todoQuery.getDeadlineFrom().isEmpty() && !todoQuery.getDeadlineTo().isEmpty()) {
			todoList = todoRepository.findByDeadlineBetweenOrderByDeadlineAsc(
				Utils.str2date(todoQuery.getDeadlineFrom()),
				Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			todoList = todoRepository.findByDone("Y");
		}

		return todoList;
	}

	public boolean isValid(TodoQuery todoQuery, BindingResult result) {
		boolean ans = true;

		String date = todoQuery.getDeadlineFrom();
		if (!date.isEmpty()) {
			try {
				LocalDate.parse(date);
			} catch (DateTimeException e) {
				FieldError fieldError = new FieldError(
					result.getObjectName(),
					"deadlineFrom",
					"期限：開始を入力するときはyyyy-mm-dd形式で入力してください");
				result.addError(fieldError);
				ans = false;
			}
		}

		date = todoQuery.getDeadlineTo();
		if (!date.isEmpty()) {
			try {
				LocalDate.parse(date);
			} catch (DateTimeException e) {
				FieldError fieldError = new FieldError(
					result.getObjectName(),
					"deadlineTo",
					"期限：終了を入力するときはyyyy-mm-dd形式で入力してください");
				result.addError(fieldError);
				ans = false;
			}
		}

		return ans;
	}
	public boolean isValid(TodoData todoData, BindingResult result) {
		boolean ans = true;

		// 件名が全角スペースだけの場合はエラー
		String title = todoData.getTitle();
		if (title != null && !title.equals("")) {
			boolean isAllDoubleSpace = true;
			for (int i = 0; i < title.length(); i++) {
				if (title.charAt(i) != '　') {
					isAllDoubleSpace = false;
					break;
				}
			}
			if (isAllDoubleSpace) {
				FieldError fieldError = new FieldError(
					result.getObjectName(),
					"title",
					"件名が全角スペースのみです");
				result.addError(fieldError);
				ans = false;
			}
		}

		// 期限チェック（過去日付はエラー）
		String deadline = todoData.getDeadline();
		if (!deadline.isEmpty()) {
			LocalDate today = LocalDate.now();
			try {
				LocalDate deadlineDate = LocalDate.parse(deadline);
				if (deadlineDate.isBefore(today)) {
					FieldError fieldError = new FieldError(
						result.getObjectName(),
						"deadline",
						"期限は今日以降の日付を入力してください");
					result.addError(fieldError);
					ans = false;
				}
			} catch (DateTimeException e) {
				FieldError fieldError = new FieldError(
					result.getObjectName(),
					"deadline",
					"期限は yyyy-mm-dd の形式で入力してください");
				result.addError(fieldError);
				ans = false;
			}
		}

		return ans;
	}
}