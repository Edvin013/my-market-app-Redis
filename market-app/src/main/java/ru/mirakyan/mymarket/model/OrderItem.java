package ru.mirakyan.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    @Transient
    private Item item;

    @Column("count")
    private Integer count;

    @Column("price")
    private Long price;

    public OrderItem(Long orderId, Long itemId, Integer count, Long price) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.count = count;
        this.price = price;
    }
}
