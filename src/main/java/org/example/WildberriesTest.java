package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.WebElement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WildberriesTest {

    ChromeOptions options = new ChromeOptions().addArguments("--remote-allow-origins=*");
    private WebDriver driver;
    Actions actions;
    List<Integer> priceInIntFormat = new ArrayList<>();
    List<String> nameInStrFormat = new ArrayList<>();

    @BeforeEach
    public void driverSetUp(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();

        // Открываем Wildberries
        driver.get("https://www.wildberries.ru/");
        actions = new Actions(driver);
    }

    @Test
    public void testWildberriesCheckout() {
        //добавление товаров в корщзину
        addItemsToCart();

        //открытие корзины
        driver.findElement(By.xpath("(//*[@class='navbar-pc__link j-wba-header-item'])[2]")).click();

        System.out.println("Сравниваем:\n");
        // Проверяем товары в корзине
        checkCartItems();
    }



    private void addItemsToCart() {

        for (int i = 1; i < 11; i++) {

            //Нажать на быстрый просмотр товара
            driver.findElement(By.xpath("(//*[@class='product-card__fast-view hide-mobile j-open-product-popup'])[" + i + "]")).click();

            //Выбираем размер товара и кладем в корзину, в случае если надо выбрать размер
            try {driver.findElement(By.xpath("(//*[@class='sizes-list__item'])[1]")).click();

                driver.findElement(By.xpath("(//*[@class='btn-main-2'])[1]")).click();

            } catch (NoSuchElementException ex){

                System.out.println();
                //Добавить товар в корзину
                driver.findElement(By.xpath("(//*[@class='btn-main-2'])[1]")).click();

            }

            //Находим цену товара
            List<WebElement> priceOfProduct = driver.findElements(By.xpath("//*[@class='price-block__final-price']"));

            //Парсим
            for (WebElement priceElement : priceOfProduct){
                String priceText = priceElement.getText().replaceAll("[^\\d.]", "");

                try {
                    int parceInInt = Integer.parseInt(priceText);
                    //кладем значение в массив
                    priceInIntFormat.add(parceInInt);
                    System.out.println(parceInInt + " рублей");
                } catch (NumberFormatException exception){
                    System.out.println(" Ошибка");
                    System.out.println(exception.getClass());
                }
            }
            //То же самое, но с названиями товаров
            List<WebElement> nameOfProduct = driver.findElements(By.xpath("(//*[@data-link='text{:selectedNomenclature^goodsName}'])"));

            for (WebElement nameElement : nameOfProduct){
                String nameText = nameElement.getText();
                nameInStrFormat.add(nameText);
                System.out.println(nameText);
                System.out.println("_______________________________");
            }

            // Подождать 3 секунды
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Закрыть
            driver.findElement(By.xpath("(//*[@class='j-close popup__close close'])[1]")).click();
        }
    }



    private void checkCartItems() {

        //суммируем цены товаров
        int sum = priceInIntFormat.stream().mapToInt(Integer::intValue).sum();
        System.out.println(sum);

        //ждем 3 секунды чтобы страница прогрузилась до конца
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //локатор итоговой цены
        String resultPrice = driver.findElement(By.xpath("//*[@class='b-right']")).getText();

        //удаляем ненужные пробелы
        String str = resultPrice.replaceAll("[^\\d.]", "");
        int parseInInt2 = Integer.parseInt(str);   //парсим

        //сравниваем названия товаров
        for (int i = 1; i < nameInStrFormat.size(); i++) {
            String actualProductName = driver.findElement(By.xpath("(//*[@class='good-info__good-name'])[" + i + "]")).getText();
            Assertions.assertEquals(actualProductName, nameInStrFormat.get((10-i)));

            System.out.println(nameInStrFormat.get(10-i) + " = ожидаемое название товара" /* nameInStrFormat */);
            System.out.println(actualProductName + " = фактическое название товара\n"   /* actualProductName */);
        }
        //сравниваем итоговые цены
        Assertions.assertEquals(sum, parseInInt2);

        System.out.println("Ожидаемая цена = " + sum + " рублей");
        System.out.println("Фактическая цена = " + parseInInt2 + " рублей");
        System.out.println("Цены равны!");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
