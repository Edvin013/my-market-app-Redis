package ru.mirakyan.mymarket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.mirakyan.mymarket.model.Item;
import ru.mirakyan.mymarket.model.User;
import ru.mirakyan.mymarket.repository.ItemRepository;
import ru.mirakyan.mymarket.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Инициализация пользователей
        initializeUsers();
        
        // Инициализация товаров
        initializeItems();
    }
    
    private void initializeUsers() {
        List<User> users = Arrays.asList(
            new User(null, "user", passwordEncoder.encode("password"), true),
            new User(null, "admin", passwordEncoder.encode("admin123"), true),
            new User(null, "test", passwordEncoder.encode("test"), true)
        );
        
        userRepository.count()
            .flatMapMany(count -> {
                if (count == 0) {
                    log.info("Инициализация базы данных пользователями...");
                    return Flux.fromIterable(users)
                        .flatMap(userRepository::save)
                        .doOnComplete(() -> log.info("Пользователи инициализированы успешно"));
                } else {
                    log.info("База данных уже содержит {} пользователей", count);
                    return Flux.empty();
                }
            })
            .subscribe();
    }
    
    private void initializeItems() {
        List<Item> items = Arrays.asList(
            new Item(null, "Футбольный мяч", "Профессиональный футбольный мяч размер 5", "/images/ball.jpg", 2500L),
            new Item(null, "Баскетбольный мяч", "Баскетбольный мяч для игры в зале", "/images/basketball.jpg", 3000L),
            new Item(null, "Теннисная ракетка", "Профессиональная теннисная ракетка", "/images/racket.jpg", 8500L),
            new Item(null, "Беговые кроссовки", "Легкие кроссовки для бега", "/images/shoes.jpg", 5500L),
            new Item(null, "Фитнес-браслет", "Умный браслет с датчиком пульса", "/images/fitness-band.jpg", 3500L),
            new Item(null, "Йога-мат", "Нескользящий коврик для йоги", "/images/yoga-mat.jpg", 1500L),
            new Item(null, "Гантели 5 кг", "Набор гантелей по 5 кг", "/images/dumbbells.jpg", 2000L),
            new Item(null, "Скакалка", "Скакалка для кардио тренировок", "/images/rope.jpg", 500L),
            new Item(null, "Велосипед горный", "Горный велосипед 21 скорость", "/images/bike.jpg", 35000L),
            new Item(null, "Плавательные очки", "Очки для плавания с защитой UV", "/images/goggles.jpg", 1200L),
            new Item(null, "Рюкзак туристический", "Вместительный рюкзак 50 литров", "/images/backpack.jpg", 6500L),
            new Item(null, "Термос", "Термос из нержавеющей стали 1 литр", "/images/thermos.jpg", 1800L),
            new Item(null, "Спортивная бутылка", "Бутылка для воды 750 мл", "/images/bottle.jpg", 600L),
            new Item(null, "Компас", "Туристический компас с жидкостью", "/images/compass.jpg", 800L),
            new Item(null, "Палатка 2-местная", "Легкая палатка для похода", "/images/tent.jpg", 12000L)
        );

        itemRepository.count()
            .flatMapMany(count -> {
                if (count == 0) {
                    log.info("Инициализация базы данных товарами...");
                    return Flux.fromIterable(items)
                        .flatMap(itemRepository::save)
                        .doOnComplete(() -> log.info("База данных товарами инициализирована успешно"));
                } else {
                    log.info("База данных уже содержит {} товаров", count);
                    return Flux.empty();
                }
            })
            .subscribe();
    }
}

