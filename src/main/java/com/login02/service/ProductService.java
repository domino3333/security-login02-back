package com.login02.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.login02.domain.Product;
import com.login02.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	
	
	private final ProductRepository productRepository;
	
	
	
	public Page<Product> findAll(Pageable pageable){
		
		return productRepository.findAll(pageable);
	}
}
