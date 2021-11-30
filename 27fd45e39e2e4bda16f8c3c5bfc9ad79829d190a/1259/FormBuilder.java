import java.util.Date;

public class FormBuilder {

    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String address;
    private Date dob;
    private String email;
    private String backupEmail;
    private String spouseName;
    private String city;
    private String state;
    private String country;
    private String language;
    private String passwordHint;
    private String securityQuestion;
    private String securityAnswer;

    public FormBuilder(String firstName, String lastName, String userName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
    }

    public FormBuilder address(String address) {
        this.address = address;
        return this;
    }

    public FormBuilder dob(Date dob) {
        this.dob = dob;
        return this;
    }

    public FormBuilder email(String email) {
        this.email = email;
        return this;
    }

    public FormBuilder backupEmail(String backupEmail) {
        this.backupEmail = backupEmail;
        return this;
    }

    public FormBuilder spouseName(String spouseName) {
        this.spouseName = spouseName;
        return this;
    }

    public FormBuilder city(String city) {
        this.city = city;
        return this;
    }

    public FormBuilder state(String state) {
        this.state = state;
        return this;
    }

    public FormBuilder country(String country) {
        this.country = country;
        return this;
    }

    public FormBuilder language(String language) {
        this.language = language;
        return this;
    }

    public FormBuilder passwordHint(String passwordHint) {
        this.passwordHint = passwordHint;
        return this;
    }

    public FormBuilder securityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
        return this;
    }

    public FormBuilder securityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
        return this;
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public Date getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public String getBackupEmail() {
        return backupEmail;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getLanguage() {
        return language;
    }

    public String getPasswordHint() {
        return passwordHint;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public Form build() {
        return new Form(this);
    }
}