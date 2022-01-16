## `Статус сборки` [![Build status](https://ci.appveyor.com/api/projects/status/p4p82pacdqrwd6ly?svg=true)](https://ci.appveyor.com/project/Lognestix/aqa-exercise-2-3-1)
## В build.gradle добавленна поддержка JUnit-Jupiter, Selenide и headless-режим, JavaFaker, Lombok.
```gradle
plugins {
    id 'java'
}

group 'ru.netology'
version '1.0-SNAPSHOT'

sourceCompatibility = 11

//Кодировка файлов (если используется русский язык в файлах)
compileJava.options.encoding = "UTF-8"
compileTestJava.options.encoding = "UTF-8"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.22'
    annotationProcessor 'org.projectlombok:lombok:1.18.22'

    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.2'
    testImplementation 'com.codeborne:selenide:6.2.0'
    testImplementation 'com.github.javafaker:javafaker:1.0.2'

    testCompileOnly 'org.projectlombok:lombok:1.18.22'
    testAnnotationProcessor 'org.projectlombok:lombok:1.18.22'
}

test {
    useJUnitPlatform()
    //В тестах, при вызове `gradlew test -Dselenide.headless=true` будет передаватся этот параметр в JVM (где его подтянет Selenide)
    systemProperty 'selenide.headless', System.getProperty('selenide.headless')
}
```
## Код Java для оптимизации авто-тестов.
```Java
package manager;

import com.github.javafaker.Faker;
import domain.UserData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

public class DataGenerator {
    private DataGenerator() {
    }

    public static String generateDate(int shift) {
        return LocalDate.now().plusDays(shift)                          //Текущая дата плюс shift дней
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));     //Формат даты день.месяц.год
    }

    public static String generateCity() {
        final Random random = new Random();
        final String [] citiesAdministrativeCenters = new String [] {"Майкоп", "Горно-Алтайск", "Уфа", "Улан-Удэ",
                "Махачкала", "Магас", "Нальчик", "Элиста", "Черкесск", "Петрозаводск", "Сыктывкар", "Симферополь",
                "Йошкар-Ола", "Саранск", "Якутск", "Владикавказ", "Казань", "Кызыл", "Ижевск", "Абакан", "Грозный",
                "Чебоксары", "Барнаул", "Чита", "Петропавловск-Камчатский", "Краснодар", "Красноярск", "Пермь",
                "Владивосток", "Ставрополь", "Хабаровск", "Благовещенск", "Архангельск", "Астрахань", "Белгород",
                "Брянск", "Владимир", "Волгоград", "Вологда", "Воронеж", "Иваново", "Иркутск", "Калининград", "Калуга",
                "Кемерово", "Киров", "Кострома", "Курган", "Курск", "Гатчина", "Липецк", "Магадан", "Красногорск",
                "Мурманск", "Нижний Новгород", "Великий Новгород", "Новосибирск", "Омск", "Оренбург", "Орёл", "Пенза",
                "Псков", "Ростов-на-Дону", "Рязань", "Самара", "Саратов", "Южно-Сахалинск", "Екатеринбург", "Смоленск",
                "Тамбов", "Тверь", "Томск", "Тула", "Тюмень", "Ульяновск", "Челябинск", "Ярославль", "Москва",
                "Санкт-Петербург", "Севастополь", "Биробиджан", "Нарьян-Мар", "Ханты-Мансийск", "Анадырь", "Салехард"};
        return citiesAdministrativeCenters[random.nextInt(85)];
    }

    public static String generateName(String locale) {
        Faker faker = new Faker(new Locale(locale));
        return faker.name().fullName();
    }

    public static String generatePhone(String locale) {
        Faker faker = new Faker(new Locale(locale));
        return faker.phoneNumber().phoneNumber();
    }

    public static class Registration {
        private Registration() {
        }

        public static UserData generateUser(String locale) {
            return new UserData(generateCity(), generateName(locale), generatePhone(locale));
        }
    }
}
```
```Java
package domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserData {
    private final String city;
    private final String name;
    private final String phone;
}
/*  Если не использовать аннотацию Data и RequiredArgsConstructor lombok, то:
  public class UserData {
    private final String city;
    private final String name;
    private final String phone;

    public UserData (String city, String name, String phone) {
       this.city = city;
       this.name = name;
       this.phone = phone;
    }

    public String getCity() { return city; }

    public String getName() { return name; }

    public String getPhone() { return phone; }
  }
 */
```
## Авто-тесты находящиеся в этом репозитории.
```Java
import com.codeborne.selenide.Condition;
import domain.UserData;
import manager.DataGenerator;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

public class CardDeliveryTest {
    //Тестируемая функциональность: если заполнить форму повторно теми же данными за исключением "Даты встречи",
    //то система предложит перепланировать время встречи.
    @Test
    public void shouldSuccessfulFormSubmission() {
        open("http://localhost:9999");
        UserData userData = DataGenerator.Registration.generateUser("Ru");
        //Заполнение и первоначальная отправка формы:
        $("[data-test-id=city] input").setValue(userData.getCity());
        $("[data-test-id=date] input").doubleClick().sendKeys(Keys.DELETE);
        String scheduledDate = DataGenerator.generateDate(4);   //Запланированная дата (текущая дата + 4 дня)
        $("[data-test-id=date] input").setValue(scheduledDate);
        $("[data-test-id=name] input").setValue(userData.getName());
        $("[data-test-id=phone] input").setValue(userData.getPhone());
        $("[data-test-id=agreement]").click();
        $(".button").shouldHave(Condition.text("Запланировать")).click();
        //Проверка на видимость, содержание текста и время загрузки:
        $("[data-test-id=success-notification]").shouldBe(Condition.visible)
                .shouldHave(Condition.text("Успешно! Встреча успешно запланирована на " + scheduledDate),
                        Duration.ofSeconds(15));
        //Изменение ранне введенной даты и отправка формы:
        $("[data-test-id=date] input").doubleClick().sendKeys(Keys.DELETE);
        String rescheduledDate = DataGenerator.generateDate(14);   //Перенесенная дата (текущая дата + 14 дней)
        $("[data-test-id=date] input").setValue(rescheduledDate);
        $(".button").shouldHave(Condition.text("Запланировать")).click();
        //Взаимодействие с опцией перепланировки,
        //а также проверка на видимость, содержание текста и время загрузки:
        $("[data-test-id=replan-notification]").shouldBe(Condition.visible)
                .shouldHave(Condition.text("Необходимо подтверждение" +
                                " У вас уже запланирована встреча на другую дату. Перепланировать?"),
                        Duration.ofSeconds(15));
        $("[data-test-id=replan-notification] .button").shouldHave(Condition.text("Перепланировать")).click();
        //Итоговая проверка на видимость, содержание текста и время загрузки:
        $("[data-test-id=success-notification]").shouldBe(Condition.visible)
                .shouldHave(Condition.text("Успешно! Встреча успешно запланирована на " + rescheduledDate),
                        Duration.ofSeconds(15));
    }
}
```