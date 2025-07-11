package com.example.demo.controller;

import java.util.List;

//import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.todolist.dao.TodoDaoImpl;
import com.example.demo.todolist.entity.Todo;
import com.example.demo.todolist.form.TodoData;
import com.example.demo.todolist.form.TodoQuery;
import com.example.demo.todolist.repository.TodoRepository;
import com.example.demo.todolist.service.TodoService;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Controller
//@AllArgsConstructor
@RequiredArgsConstructor
public class TodoListController {
	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;
	
	//20250703追加
	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;
	
	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}
	//Todo一覧表示　ページ対応
	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv,
			@PageableDefault(page = 0, size = 5, sort ="id")Pageable pageable) {
		//一覧を検索して表示する
		mv.setViewName("todoList");
		//Page<Todo> todoList = todoRepository.findAll(pageable);
		Page<Todo> todoPage = todoRepository.findAll(pageable);
		mv.addObject("todoQuery", new TodoQuery());
		//mv.addObject("todoList", todoList);
		mv.addObject("todoPage", todoPage);
		mv.addObject("todoList", todoPage.getContent());
		session.setAttribute("todoQuery", new TodoQuery());
		return mv;
	}
//旧
//	@GetMapping("/todo")
//	public ModelAndView showTodoList(ModelAndView mv) {
//		//一覧を検索して表示する
//		mv.setViewName("todoList");
//		List<Todo> todoList = todoRepository.findAll();
//		mv.addObject("todoList", todoList);
//		mv.addObject("todoQuery", new TodoQuery());
//		return mv;
//	}
	@GetMapping("/todo/query")
	public ModelAndView queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable,
									ModelAndView mv) {
		mv.setViewName("todoList");
		
		//sessionに保存されている条件で検索
		TodoQuery todoQuery = (TodoQuery)session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
		
		mv.addObject("todoQuery", todoQuery); //検索条件表示用
		mv.addObject("todoPage", todoPage); //page情報
		mv.addObject("todoList", todoPage.getContent()); //検索結果
		return mv;
	}
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery,
								BindingResult result,
								@PageableDefault(page = 0, size = 5) Pageable pageable,
								ModelAndView mv) {
		mv.setViewName("todoList");
		Page<Todo> todoPage = null;
		//List<Todo> todoPage = null;
		//List<Todo> todoList = null;
		if(todoService.isValid(todoQuery, result)) {
			//エラーがなければ検索
			todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
			session.setAttribute("todoQuery", todoQuery);
			//入力された条件をsessionに保存
			mv.addObject("todoPage", todoPage);
			mv.addObject("todoList", todoPage.getContent());
			//todoList = todoService.doQuery(todoQuery);
			//↓
			//JPQLによる検索
			//todoList=todoDaoImpl.findByJPQL(todoQuery);
			//todoPage=todoDaoImpl.findByJPQL(todoQuery);
			//mv.addObject("todoQuery", todoQuery);		
		} else {
			mv.addObject("todoPage", null);
			mv.addObject("todoList", null);
		}
			//エラーがあった場合検索
			//mv.addObject("todoList", null);
		return mv;
	}
	
	//Todo入力フォーム表示
	//Todo一覧画面で「新規追加」がクリックされたとき
	@GetMapping("/todo/create/form")
	public ModelAndView createTodo(ModelAndView mv) {
		mv.setViewName("todoForm");
		mv.addObject("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return mv;
	}
	
	@PostMapping("/todo/create/form")
	public ModelAndView createTodoPost(ModelAndView mv) {
	    return createTodo(mv); // GET用と同じ画面を返す
	}
	
	//ToDo追加処理
	//Todo入力画面で「登録」がクリックされたとき
	@PostMapping("/todo/create")
	public String createTodo(@ModelAttribute @Validated TodoData todoData,
									BindingResult result,
									Model model) {
		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if(!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			//エラーあり
			return "todoForm";	
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
	
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
		
	}
	
}
