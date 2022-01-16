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