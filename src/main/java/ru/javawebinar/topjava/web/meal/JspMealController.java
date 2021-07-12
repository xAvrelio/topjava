package ru.javawebinar.topjava.web.meal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.javawebinar.topjava.model.Meal;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static ru.javawebinar.topjava.util.DateTimeUtil.parseLocalDate;
import static ru.javawebinar.topjava.util.DateTimeUtil.parseLocalTime;

@Controller
@RequestMapping("/meals")
public class JspMealController extends AbstractMealController {

    @PostMapping
    public String doPost(HttpServletRequest request) {
        Meal meal = new Meal(
                LocalDateTime.parse(request.getParameter("dateTime")),
                request.getParameter("description"),
                Integer.parseInt(request.getParameter("calories")));

        if (StringUtils.hasLength(request.getParameter("id"))) {
            super.update(meal, Integer.parseInt(request.getParameter("id")));
        } else {
            super.create(meal);
        }
        return "redirect:/meals";
    }


    @GetMapping("/create")
    public String createMeal(Model model){
        Meal meal = new Meal(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), "", 1000);
        model.addAttribute("meal", meal);
        return "mealForm";
    }

    @GetMapping("/update/{id}")
    public String updateMeal(@PathVariable Integer id, Model model){
        Objects.requireNonNull(id);
        Meal meal = super.get(id);
        model.addAttribute("meal", meal);
        return "mealForm";
    }


    @GetMapping("/delete/{id}")
    public String deleteMeal(@PathVariable Integer id){
        Objects.requireNonNull(id);
        super.delete(id);
        return "redirect:/meals";
    }


    @GetMapping("/filter")
    public String getMeals(HttpServletRequest request, Model model)  {
        LocalDate LocalStartDate = parseLocalDate(request.getParameter("startDate"));
        LocalDate LocalEndDate = parseLocalDate(request.getParameter("endDate"));
        LocalTime LocalStartTime = parseLocalTime(request.getParameter("startTime"));
        LocalTime LocalEndTime = parseLocalTime(request.getParameter("endTime"));
        model.addAttribute("meals", super.getBetween(LocalStartDate, LocalStartTime, LocalEndDate, LocalEndTime));
        return "meals";
    }

}
