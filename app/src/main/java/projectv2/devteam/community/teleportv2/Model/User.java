package projectv2.devteam.community.teleportv2.Model;

/**
 * Created by Santos on 4/27/2018.
 */

public class User {
    private String email,password,name,phone,motorcycle,plateNumber,teamName,avatarUrl,refCode,rates;

    public User(){
    }


    public User(String email, String password, String name, String phone, String motorcycle, String plateNumber, String teamName, String avatarUrl, String refCode, String rates) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.motorcycle = motorcycle;
        this.plateNumber = plateNumber;
        this.teamName = teamName;
        this.avatarUrl = avatarUrl;
        this.refCode = refCode;
        this.rates = rates;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMotorcycle() {
        return motorcycle;
    }

    public void setMotorcycle(String motorcycle) {
        this.motorcycle = motorcycle;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }
}
