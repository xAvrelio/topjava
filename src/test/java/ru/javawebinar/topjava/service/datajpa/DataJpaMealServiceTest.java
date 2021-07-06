package ru.javawebinar.topjava.service.datajpa;


import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.service.AbstractMealServiceTest;
import ru.javawebinar.topjava.service.MealService;



import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.Profiles.DATAJPA;


@ActiveProfiles(DATAJPA)
public class DataJpaMealServiceTest extends AbstractMealServiceTest {


    @Test
    public void getMealsByUserId() {
        MealService service = getService();
        Meal meal = service.getMealWithUserByIdAndUserId(ADMIN_MEAL_ID, UserTestData.ADMIN_ID);
        User actualUser = meal.getUser();
        UserTestData.MATCHER.assertMatch(actualUser, UserTestData.admin);
    }


}
