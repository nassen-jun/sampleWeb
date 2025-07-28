package com.example.demo.controller;

import java.util.List;

//import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
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
	
	private String redirectToCurrentPage() {
	    TodoQuery query = (TodoQuery) session.getAttribute("todoQuery");
	    int currentPage = (query != null ) ? query.getCurrentPage() : 0;
	    
	    long totalCount = todoRepository.count();
	    int pageSize = 5;
	    int maxPage = (int)Math.max((totalCount - 1) / pageSize, 0);
	    
	    if(currentPage > maxPage) {
	    	currentPage = maxPage;
	    	if(query != null) {
	    		query.setCurrentPage(currentPage);
	    		session.setAttribute("todoQuery", query);
	    	}
	    }
/*	    int currentPage;
	    if(query != null) {
	    	currentPage = query.getCurrentPage();
	    } else {
	    	currentPage = 0;
	    }*/
	    return "redirect:/todo?page=" + currentPage;
	}
	
	//Todo一覧表示　ページ対応
	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv,
			@PageableDefault(page = 0, size = 5, sort ="id")Pageable pageable) {
		mv.setViewName("todoList");
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		if(todoQuery == null) {
			todoQuery = new TodoQuery();
			session.setAttribute("todoQuery", todoQuery);
		}
		//	    HttpSession session = this.session; // 明示的に扱う
	    System.out.println("▶️ セッションID: " + session.getId());
	    System.out.println("🧪 todoQuery is: " + session.getAttribute("todoQuery"));
		//一覧を検索して表示する
		mv.setViewName("todoList");
		//Page<Todo> todoList = todoRepository.findAll(pageable);
		Page<Todo> todoPage = todoRepository.findAll(pageable);
		mv.addObject("todoQuery", todoQuery);
		mv.addObject("todoPage", todoPage);
		mv.addObject("todoList", todoPage.getContent());
		//mv.addObject("todoList", todoList);
		//session.setAttribute("todoQuery", new TodoQuery());
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

	    TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
	    if (todoQuery == null) {
	        todoQuery = new TodoQuery();
	        session.setAttribute("todoQuery", todoQuery);
	    }

	    Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);

	    // 💥 null チェックと空ページの防御（ここ重要！）
	    if (todoPage == null || todoPage.getTotalElements() == 0) {
	        todoPage = Page.empty(pageable); // ← 必ず空ページを渡す
	    }

	    int requestedPage = pageable.getPageNumber();
	    int totalPages = todoPage.getTotalPages();

	    // 💡 リダイレクトで無限ループしないようにガード
	    if (totalPages > 0 && requestedPage >= totalPages) {
	        return new ModelAndView("redirect:/todo/query?page=" + (totalPages - 1));
	    }

	    mv.addObject("todoQuery", todoQuery);
	    mv.addObject("todoPage", todoPage); // ← 渡し忘れないこと！
	    mv.addObject("todoList", todoPage.getContent());

	    return mv;
	}
	
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery,
	                              BindingResult result,
	                              @PageableDefault(page = 0, size = 5) Pageable pageable,
	                              ModelAndView mv) {
	    mv.setViewName("todoList");
	    Page<Todo> todoPage = null;

	    if (todoService.isValid(todoQuery, result)) {
	        todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
	        session.setAttribute("todoQuery", todoQuery);
	        mv.addObject("todoPage", todoPage);
	        mv.addObject("todoList", todoPage.getContent());
	    } else {
	        todoPage = Page.empty(pageable); // ✅ 空ページを明示的に渡す
	        mv.addObject("todoPage", todoPage);
	        mv.addObject("todoList", todoPage.getContent());
	    }

	    return mv;
	}
	
	//Todo入力フォーム表示
	//Todo一覧画面で「新規追加」がクリックされたとき
	@GetMapping("/todo/create/form")
	public ModelAndView createTodo(ModelAndView mv) {
		TodoData data = new TodoData();
		Integer maxId = todoRepository.findMaxId();
		int nextId = (maxId != null) ? maxId +1 : 1;
		data.setTitle("todo-" + nextId);
		
		mv.setViewName("todoForm");
		mv.addObject("todoData", data);
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
		String mode = (String)session.getAttribute("mode");
		boolean isValid = todoService.isValid(todoData, result, mode);
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
		System.out.println("todoQuery in session: " + session.getAttribute("todoQuery"));
		return redirectToCurrentPage();
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
		String mode = (String) session.getAttribute("mode");
		boolean isValid = todoService.isValid(todoData, result, mode);
		if(!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return redirectToCurrentPage();
		} else {
			return "todoForm";
		}
	}
	
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return redirectToCurrentPage();
	}
	
	@PostMapping("/todo/deleteChecked")
	public String deleteChecked(@RequestParam(name = "ids", required = false) List<Integer> ids) {
		if(ids != null) {
			for(Integer id : ids) {
				todoRepository.deleteById(id);
			}
		}
		return redirectToCurrentPage();
	}
}
