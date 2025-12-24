package ru.mirakyan.mymarket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Transient
    private List<OrderItem> items = new ArrayList<>();

    @Column("total_sum")
    private Long totalSum;

    public Order(Long userId, Long totalSum) {
        this.userId = userId;
        this.totalSum = totalSum;
        this.items = new ArrayList<>();
    }
}
