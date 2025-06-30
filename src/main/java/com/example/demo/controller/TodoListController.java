package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.todolist.entity.Todo;
import com.example.demo.todolist.form.TodoData;
import com.example.demo.todolist.repository.TodoRepository;
import com.example.demo.todolist.service.TodoService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class TodoListController {
	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;
	
	//Todo一覧表示
	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv) {
		//一覧を検索して表示する
		mv.setViewName("todoList");
		List<Todo> todoList = todoRepository.findAll();
		mv.addObject("todoList", todoList);
		return mv;
	}

	//Todo入力フォーム表示
	//Todo一覧画面で「新規追加」がクリックされたとき
	@GetMapping("/todo/create")
	public ModelAndView createTodo(ModelAndView mv) {
		mv.setViewName("todoForm");
		mv.addObject("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return mv;
	}
	
	//ToDo追加処理
	//Todo入力画面で「登録」がクリックされたとき
	@PostMapping("/todo/create")
	public ModelAndView createTodo(@ModelAttribute @Validated TodoData todoData,
									BindingResult result,
									ModelAndView mv) {
		boolean isValid = todoService.isValid(todoData, result);
		if(!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return showTodoList(mv);
		} else {
			//エラーあり
			mv.setViewName("todoForm");
			return mv;	
		}
	}
	//ToDo一覧に戻る
	//ToDo入力画面で「キャンセル登録」ボタンがクリックされたとき
	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo";
	}
	
	@GetMapping("/todo/{id}")
	public ModelAndView todoById(@PathVariable(name = "id")int id, ModelAndView mv) {
		mv.setViewName("todoForm");
		Todo todo = todoRepository.findById(id).get();
		mv.addObject("todoData", todo);
		session.setAttribute("mode", "update");
		return mv;
		
	}
	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData,
							BindingResult result, Model model) {
		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if(!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			return "todoForm";
		}
		
	}
	
	
}
