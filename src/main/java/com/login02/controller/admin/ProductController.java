package com.login02.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.login02.domain.Product;

import lombok.extern.java.Log;

@Controller
@Log
@RequestMapping("/admin/product")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

	@Value("${upload.path}")
	private String uploadPath;
	
	@PostMapping("/add")
	public ResponseEntity<?> add(String productName, int productQuantity,
			@RequestParam(value = "productImage", required = false) MultipartFile productImage) {
		String savedFileName = null;

	    // 1. 이미지 저장
	    if (productImage != null && !productImage.isEmpty()) {
	        try {
	            String originalName = productImage.getOriginalFilename();
	            String uuid = UUID.randomUUID().toString();
	            savedFileName = uuid + "_" + originalName;
	            Path path = Paths.get(uploadPath, savedFileName);
	            Files.write(path, productImage.getBytes());
	        } catch (IOException e) {
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 저장 실패");
	        }
	    }

	    // 2. 상품 DB 저장
	    Product product = new Product();
	    product.setProductName(productName);
	    product.setProductQuantity(productQuantity);
	    product.setImagePath(savedFileName); // DB에는 파일명만 저장
	    productRepository.save(product);

	    return ResponseEntity.ok("상품 추가 완료");

	}
}
