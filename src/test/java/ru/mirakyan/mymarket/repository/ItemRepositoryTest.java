package ru.mirakyan.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mirakyan.mymarket.model.Item;

import java.util.List;

@DataR2dbcTest
@TestPropertySource(properties = "spring.sql.init.mode=always")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll().block();
    }

    @Test
    void testSaveItem() {
        Item item = new Item(null, "Test Item", "Test Description", "/img/test.jpg", 100L);

        StepVerifier.create(itemRepository.save(item))
                .assertNext(saved -> {
                    assert saved.getId() != null : "ID должен быть не null";
                    assert saved.getTitle().equals("Test Item") : "Title должен быть Test Item";
                    assert saved.getDescription().equals("Test Description") : "Description должен быть Test Description";
                    assert saved.getImgPath().equals("/img/test.jpg") : "ImgPath должен быть /img/test.jpg";
                    assert saved.getPrice() == 100L : "Price должен быть 100L";
                })
                .verifyComplete();
    }

    @Test
    void testFindById() {
        Item item = new Item(null, "Test Item", "Test Description", "/img/test.jpg", 100L);
        Mono<Item> savedMono = itemRepository.save(item);

        StepVerifier.create(savedMono.flatMap(saved -> itemRepository.findById(saved.getId())))
                .assertNext(found -> {
                    assert found.getTitle().equals("Test Item") : "Title должен быть Test Item";
                    assert found.getPrice() == 100L : "Price должен быть 100L";
                })
                .verifyComplete();
    }

    @Test
    void testFindAll() {
        Item item1 = new Item(null, "Item 1", "Description 1", "/img1.jpg", 100L);
        Item item2 = new Item(null, "Item 2", "Description 2", "/img2.jpg", 200L);

        StepVerifier.create(
                itemRepository.save(item1)
                        .then(itemRepository.save(item2))
                        .thenMany(itemRepository.findAll())
        )
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void testFindByTitleOrDescriptionContaining_SearchInTitle() {
        Item item1 = new Item(null, "Laptop", "Gaming laptop", "/img1.jpg", 1000L);
        Item item2 = new Item(null, "Mouse", "Gaming mouse", "/img2.jpg", 50L);
        Item item3 = new Item(null, "Keyboard", "Mechanical keyboard", "/img3.jpg", 150L);

        StepVerifier.create(
                itemRepository.save(item1)
                        .then(itemRepository.save(item2))
                        .then(itemRepository.save(item3))
                        .thenMany(itemRepository.findByTitleOrDescriptionContaining("laptop"))
        )
                .assertNext(found -> {
                    assert found.getTitle().equals("Laptop") : "Title должен быть Laptop";
                })
                .verifyComplete();
    }

    @Test
    void testFindByTitleOrDescriptionContaining_SearchInDescription() {
        Item item1 = new Item(null, "Laptop", "Gaming laptop", "/img1.jpg", 1000L);
        Item item2 = new Item(null, "Mouse", "Gaming mouse", "/img2.jpg", 50L);
        Item item3 = new Item(null, "Keyboard", "Mechanical keyboard", "/img3.jpg", 150L);

        StepVerifier.create(
                itemRepository.save(item1)
                        .then(itemRepository.save(item2))
                        .then(itemRepository.save(item3))
                        .thenMany(itemRepository.findByTitleOrDescriptionContaining("gaming"))
        )
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void testFindByTitleOrDescriptionContaining_CaseInsensitive() {
        Item item = new Item(null, "Laptop", "Gaming Laptop", "/img.jpg", 1000L);

        StepVerifier.create(
                itemRepository.save(item)
                        .thenMany(itemRepository.findByTitleOrDescriptionContaining("LAPTOP"))
        )
                .assertNext(found -> {
                    assert found.getTitle().equals("Laptop") : "Title должен быть Laptop";
                })
                .verifyComplete();
    }

    @Test
    void testFindByTitleOrDescriptionContaining_NoMatch() {
        Item item = new Item(null, "Laptop", "Gaming laptop", "/img.jpg", 1000L);

        StepVerifier.create(
                itemRepository.save(item)
                        .thenMany(itemRepository.findByTitleOrDescriptionContaining("phone"))
        )
                .verifyComplete();
    }

    @Test
    void testDeleteItem() {
        Item item = new Item(null, "Test Item", "Test Description", "/img.jpg", 100L);

        StepVerifier.create(
                itemRepository.save(item)
                        .flatMap(saved -> itemRepository.deleteById(saved.getId())
                                .then(itemRepository.findById(saved.getId())))
        )
                .verifyComplete();
    }

    @Test
    void testUpdateItem() {
        Item item = new Item(null, "Test Item", "Test Description", "/img.jpg", 100L);

        StepVerifier.create(
                itemRepository.save(item)
                        .flatMap(saved -> {
                            saved.setTitle("Updated Item");
                            saved.setPrice(200L);
                            return itemRepository.save(saved);
                        })
        )
                .assertNext(updated -> {
                    assert updated.getTitle().equals("Updated Item") : "Title должен быть Updated Item";
                    assert updated.getPrice() == 200L : "Price должен быть 200L";
                })
                .verifyComplete();
    }
}

