package com.login02.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.login02.domain.Product;
import com.login02.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {

	private final ProductService ps;
	
	@GetMapping("/read/product")
	public ResponseEntity<?> sadf (
			@RequestParam int page,
			@RequestParam int size
			){
		
		Page<Product> result=  ps.findAll(PageRequest.of(page, size));
		
		
		return ResponseEntity
				.ok(result.getContent());
	}
}
