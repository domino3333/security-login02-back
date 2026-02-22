package com.login02.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.login02.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{



}
