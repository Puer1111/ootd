package com.ootd.ootd;

import com.ootd.ootd.model.entity.category.Category;
import com.ootd.ootd.model.entity.coupon.Coupon;
import com.ootd.ootd.repository.category.CategoryRepository;
import com.ootd.ootd.repository.coupon.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class OotdApplicationTests {

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void checkData() {
		List<Coupon> coupons = couponRepository.findAll();
		List<Category> categories = categoryRepository.findAll();

		System.out.println("Coupons: " + coupons.size());
		System.out.println("Categories: " + categories.size());

		assert !coupons.isEmpty();
		assert !categories.isEmpty();
	}

}
