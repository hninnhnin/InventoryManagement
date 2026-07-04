package org.example.repository;

import org.example.entity.Customer;
import org.example.entity.CustomerDropdownDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // ဖုန်းနံပါတ်ဖြင့် ဝယ်သူအား အမြန်ရှာဖွေရန် (Query အလိုအလျောက် ထွက်လာပါမည်)
    Optional<Customer> findByPhone(String phone);
}

