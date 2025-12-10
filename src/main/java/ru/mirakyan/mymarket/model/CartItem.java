package ru.mirakyan.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    private Long id;

    @Column("item_id")
    private Long itemId;

    @Transient
    private Item item;

    @Column("count")
    private Integer count;

    public CartItem(Long itemId, Integer count) {
        this.itemId = itemId;
        this.count = count;
    }
}
