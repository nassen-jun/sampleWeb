package com.example.demo;

import lombok.Data;

@Data
public class RegistData {
	private String name;
	private String password;
	private int gender;
	private int area;
	private int[] interest;
	private String remarks;
}
