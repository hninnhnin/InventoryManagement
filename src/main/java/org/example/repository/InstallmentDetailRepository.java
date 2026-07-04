package org.example.repository;

import org.example.entity.InstallmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallmentDetailRepository extends JpaRepository<InstallmentDetail, Long> {
    // JpaRepository ကနေ လက်ခံထားတာမို့လို့ အခြေခံ CRUD and Find Method တွေ အကုန်အဆင်သင့် သုံးလို့ရပါပြီ
}
