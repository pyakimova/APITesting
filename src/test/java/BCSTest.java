import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;

public class BCSTest {
    /*
    Тест проверяет при апдейте созданной entity:
    - код ошибки, в случае, если в поле sex попадает значение "М" на латиннице
    - после неудачного апдейта данные entity неизменны
    - entity можно успешно изменить далее
     */
    @Test
    public void entityUpdateTest() {
        // test data and parameters
        String path = "https://api.ru/test-api/api/v4/entity";
        String firstName = "firstName1 firstName2";
        String lastName = "lastName1 lastName2";
        String birthDate = "2004-05-24T04:19:52.589Z";
        String sex = "Ж";
        String firstId = "";
        String secondId = "";
        String entityJSON = "";
        String createDate = "";
        boolean active = Boolean.parseBoolean(null);
        Integer count = null;
        boolean adulthood = Boolean.parseBoolean(null);

        Map<String, String> data;
        data = new HashMap<>();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("birthDate", birthDate);
        data.put("sex", sex);

        //create entity with data
        ValidatableResponse response;
        response = given()
                .contentType("application/json")
                .body(data)
                .when()
                .post(path)
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .contentType(ContentType.JSON);

        //extract parameters of created entity from response
        firstId = response.extract().path("firstId");
        secondId = response.extract().path("secondId");

        //get entity
        given()
                .contentType("application/json")
                .when()
                .get(path + "/?firstId=" + firstId + "&seconId=" + secondId)
                .then()
                .statusCode(200).contentType(ContentType.JSON);

        //extract parameters of entity from response
        createDate = response.extract().path("createDate");
        active = response.extract().path("active");
        count = response.extract().path("count");
        adulthood = response.extract().path("entityAdditionallInfo.adulthood");


        // edited test data, sex = latin "M"
        String firstNameEdited = "firstNameEdited";
        String lastNameEdited = "lastNameEdited";
        String birthDateEdited = "2020-05-24T04:19:52.589Z";
        String sexEdited = "M";
        data.put("firstId", firstId);
        data.put("firstName", firstNameEdited);
        data.put("lastName", lastNameEdited);
        data.put("birthDate", birthDateEdited);
        data.put("sex", sexEdited);

         //edit entity with invalid sex data
        given()
                .contentType("application/json")
                .body(data)
                .when()
                .patch(path)
                .then()
                .statusCode(422);

        //get entity and check unchanged
        given()
                .contentType("application/json")
                .when()
                .get(path + "/?firstId=" + firstId + "&seconId=" + secondId)
                .then()
                .statusCode(200).contentType(ContentType.JSON)
                .body("firstId", equalTo(firstId))
                .body("secondId", equalTo(secondId))
                .body("createDate", equalTo(createDate))
                .body("active", equalTo(active))
                .body("count", equalTo(count))
                .body("entityAdditionallInfo.lastName", equalTo(lastName))
                .body("entityAdditionallInfo.firstName", equalTo(firstName))
                .body("entityAdditionallInfo.sex", equalTo(sex))
                .body("entityAdditionallInfo.birthDate", equalTo(birthDate))
                .body("entityAdditionallInfo.adulthood", equalTo(adulthood));

        // edited test data, sex = cirillic "М"
        sexEdited = "М";
        data.put("sex", sexEdited);

        //edit entity with data
        given()
                .contentType("application/json")
                .body(data)
                .when()
                .patch(path)
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .contentType(ContentType.JSON);

        //get entity and check changed
        given()
                .contentType("application/json")
                .when()
                .get(path + "/?firstId=" + firstId + "&seconId=" + secondId)
                .then()
                .statusCode(200).contentType(ContentType.JSON)
                .body("firstId", equalTo(firstId))
                .body("secondId", equalTo(secondId))
                .body("createDate", equalTo(createDate))
                .body("active", equalTo(active))
                .body("count", equalTo(count))
                .body("entityAdditionallInfo.lastName", equalTo(lastNameEdited))
                .body("entityAdditionallInfo.firstName", equalTo(firstNameEdited))
                .body("entityAdditionallInfo.sex", equalTo(sexEdited))
                .body("entityAdditionallInfo.birthDate", equalTo(birthDateEdited))
                .body("entityAdditionallInfo.adulthood", equalTo(adulthood));
    }

}
