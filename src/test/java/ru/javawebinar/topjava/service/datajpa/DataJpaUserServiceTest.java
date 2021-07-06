package ru.javawebinar.topjava.service.datajpa;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.service.AbstractUserServiceTest;
import ru.javawebinar.topjava.service.UserService;

import java.util.List;

import static ru.javawebinar.topjava.MealTestData.adminMeal1;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.Profiles.DATAJPA;


@ActiveProfiles(DATAJPA)
public class DataJpaUserServiceTest extends AbstractUserServiceTest {

    @Test
    public void getMealsByUserId() {
        UserService service = getService();
        User user = service.getUserWithMealsByUserId(UserTestData.ADMIN_ID);
        List<Meal> adminMeals = user.getMeals();
        MATCHER.assertMatch(adminMeals, adminMeal2, adminMeal1);
    }
}
