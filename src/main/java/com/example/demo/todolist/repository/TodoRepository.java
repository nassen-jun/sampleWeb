package com.example.demo.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.todolist.entity.Todo;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer>{
}
