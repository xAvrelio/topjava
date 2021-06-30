package ru.javawebinar.topjava.repository.inmemory;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryMealRepository implements MealRepository {
    public static final AtomicInteger counter = new AtomicInteger(-1);
    public static List<Meal> meals = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Meal save(Meal meal) {
        if (meal.getId() == -1) {
            meal.setId(counter.incrementAndGet());
            meals.add(meal);
        } else {
            for (Meal meal1 : meals) {
                if (meal1.getId().equals(meal.getId())) {
                    meals.set(meals.indexOf(meal1), meal);
                    return meal;
                }
            }
        }
        return null;
    }

    @Override
    public boolean delete(int id) {
        return meals.removeIf(meal -> meal.getId().equals(id));
    }

    @Override
    public Meal get(int id) {
        for (Meal meal : meals) {
            if (meal.getId().equals(id)) {
                return meal;
            }
        }
        return null;
    }

    @Override
    public List<Meal> getAll() {
        return meals;
    }

}
